/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1
 *
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 *
 * any use of this file implies acceptance of the conditions of this license.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package eu.eidas.node.auth.connector;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.SpType;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.BinaryRequestMessage;
import eu.eidas.auth.commons.tx.AuthenticationExchange;
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredLightRequest;
import eu.eidas.auth.engine.core.eidas.SPType;
import eu.eidas.node.NodeParameterNames;

import static java.lang.Boolean.parseBoolean;

/**
 * The AUCONNECTOR class serves as the middle-man in the communications between the Service Provider and the eIDAS
 * ProxyService. It is responsible for handling the requests coming from the Service Provider and forward them to the
 * eIDAS ProxyService, and vice-versa.
 *
 * @see ICONNECTORService
 */
public final class AUCONNECTOR implements ICONNECTORService {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AUCONNECTOR.class);

    /**
     * Service for country related operations.
     */
    private ICONNECTORCountrySelectorService countryService;

    /**
     * Service for SAML related operations.
     */
    private ICONNECTORSAMLService samlService;

    /**
     * Default SP Application.
     */
    private String spApplication;

    /**
     * Default SP Country.
     */
    private String spCountry;

    /**
     * Default SP Institution.
     */
    private String spInstitution;

    /**
     * Default SP Sector.
     */
    private String spSector;

    /**
     * Connector configuration.
     */
    private AUCONNECTORUtil connectorUtil;

    private CorrelationMap<StoredLightRequest> specificSpRequestCorrelationMap;

    private CorrelationMap<StoredAuthenticationRequest> connectorRequestCorrelationMap;

    /**
     * {@inheritDoc}
     */
    @Override
    public IRequestMessage getAuthenticationRequest(WebRequest webRequest, ILightRequest lightRequest) {

        IAuthenticationRequest serviceProviderRequest = samlService.processSpRequest(lightRequest, webRequest);

        String citizenIpAddress = webRequest.getRemoteIpAddress();
        String relayState = webRequest.getEncodedLastParameterValue(NodeParameterNames.RELAY_STATE.toString());

        IAuthenticationRequest fullRequest = prepareEidasRequest((IEidasAuthenticationRequest) serviceProviderRequest);

        // generate SAML Token
        IRequestMessage connectorRequest = samlService.generateServiceAuthnRequest(webRequest, fullRequest);

        byte[] samlToken = connectorRequest.getMessageBytes();

        connectorRequest = new BinaryRequestMessage(connectorRequest.getRequest(), sendRedirect(samlToken));

        String connectorRequestSamlId = connectorRequest.getRequest().getId();

        specificSpRequestCorrelationMap.put(connectorRequestSamlId, StoredLightRequest.builder()
                .request(lightRequest)
                .remoteIpAddress(citizenIpAddress)
                .relayState(relayState)
                .build());

        connectorRequestCorrelationMap.put(connectorRequestSamlId, StoredAuthenticationRequest.builder()
                .request(connectorRequest.getRequest())
                .remoteIpAddress(citizenIpAddress)
                .relayState(relayState)
                .build());

        return connectorRequest;
    }

    private IEidasAuthenticationRequest prepareEidasRequest(IEidasAuthenticationRequest authData) {
        if (connectorUtil != null && !parseBoolean(
                connectorUtil.getConfigs().getProperty(EIDASValues.DISABLE_CHECK_MANDATORY_ATTRIBUTES.toString())) &&
                !samlService.checkMandatoryAttributes(authData.getRequestedAttributes())) {
            LOG.error("BUSINESS EXCEPTION : incomplete mandatory set");
            throw new EidasNodeException(EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorCode()),
                                         EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorMessage()));
        }
        LOG.trace("do not fill in the assertion url");

        EidasAuthenticationRequest.Builder builder = EidasAuthenticationRequest.builder(authData);
        boolean modified = false;

        if (null != authData.getAssertionConsumerServiceURL()) {
            builder.assertionConsumerServiceURL(null);
            modified = true;
        }
        if (null != authData.getBinding()) {
            builder.binding(null);
            modified = true;
        }

        if (connectorUtil != null && StringUtils.isNotBlank(
                connectorUtil.getConfigs().getProperty(EIDASValues.EIDAS_SPTYPE.toString()))) {
            //only the SPType in the connector's metadata will remain active

            if (null != authData.getSpType()) {
                builder.spType((SpType) null);
                modified = true;
            }

        } else if (null == authData.getSpType()) {
            builder.spType(SPType.DEFAULT_VALUE);
            modified = true;
        }

        if (modified) {
            return builder.build();
        }

        return authData;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    public AuthenticationExchange getAuthenticationResponse(@Nonnull WebRequest webRequest) {

        // processing the webRequest
        AuthenticationExchange authenticationExchange =
                samlService.processProxyServiceResponse(webRequest, connectorRequestCorrelationMap,
                                                        specificSpRequestCorrelationMap);

        IAuthenticationResponse connectorResponse = authenticationExchange.getConnectorResponse();

        // do not validate mandatory attributes in case of failure
        if (connectorResponse.isFailure()) {
            return authenticationExchange;
        }

        ImmutableAttributeMap attributeMap = connectorResponse.getAttributes();

        boolean disableCheckMandatoryAttributes = parseBoolean(
                connectorUtil.getConfigs().getProperty(EIDASValues.DISABLE_CHECK_MANDATORY_ATTRIBUTES.toString()));

        if (!disableCheckMandatoryAttributes && !samlService.checkMandatoryAttributes(attributeMap)) {
            throw new EidasNodeException(EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorCode()),
                                         EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorMessage()));
        }
        return authenticationExchange;
    }

    /**
     * Encodes, {@link org.bouncycastle.util.encoders.Base64}, a SAML Token.
     *
     * @param samlToken The Saml Token to encode.
     * @return The encoded SAML Token.
     */
    public byte[] sendRedirect(byte[] samlToken) {
        LOG.trace("Setting attribute SAML_TOKEN_PARAM");
        return Base64.encode(samlToken);
    }

    /**
     * Setter for countryService.
     *
     * @param theCountryService The countryService to set.
     * @see ICONNECTORCountrySelectorService
     */
    public void setCountryService(ICONNECTORCountrySelectorService theCountryService) {

        this.countryService = theCountryService;
    }

    /**
     * Getter for countryService.
     *
     * @return The countryService value.
     * @see ICONNECTORCountrySelectorService
     */
    public ICONNECTORCountrySelectorService getCountryService() {
        return countryService;
    }

    /**
     * Setter for samlService.
     *
     * @param theSamlService The samlService to set.
     * @see ICONNECTORSAMLService
     */
    public void setSamlService(ICONNECTORSAMLService theSamlService) {
        this.samlService = theSamlService;
    }

    /**
     * Getter for samlService.
     *
     * @return The samlService value.
     * @see ICONNECTORSAMLService
     */
    public ICONNECTORSAMLService getSamlService() {
        return samlService;
    }

    /**
     * Getter for spApplication.
     *
     * @return The spApplication value.
     */
    public String getSpApplication() {
        return spApplication;
    }

    /**
     * Setter for default spApplication.
     *
     * @param nSpApplication The new spApplication value.
     */
    public void setSpApplication(String nSpApplication) {
        this.spApplication = nSpApplication;
    }

    /**
     * Setter for default spCountry.
     *
     * @param nSpCountry The new spCountry value.
     */
    public void setSpCountry(String nSpCountry) {
        this.spCountry = nSpCountry;
    }

    /**
     * Setter for default spInstitution.
     *
     * @param nSpInstitution The new spInstitution value.
     */
    public void setSpInstitution(String nSpInstitution) {
        this.spInstitution = nSpInstitution;
    }

    /**
     * Setter for default spSector.
     *
     * @param nSpSector The new spSector value.
     */
    public void setSpSector(String nSpSector) {
        this.spSector = nSpSector;
    }

    public void setConnectorUtil(AUCONNECTORUtil connectorUtil) {
        this.connectorUtil = connectorUtil;
    }

    public CorrelationMap<StoredLightRequest> getSpecificSpRequestCorrelationMap() {
        return specificSpRequestCorrelationMap;
    }

    public void setSpecificSpRequestCorrelationMap(CorrelationMap<StoredLightRequest> specificSpRequestCorrelationMap) {
        this.specificSpRequestCorrelationMap = specificSpRequestCorrelationMap;
    }

    public CorrelationMap<StoredAuthenticationRequest> getConnectorRequestCorrelationMap() {
        return connectorRequestCorrelationMap;
    }

    public void setConnectorRequestCorrelationMap(CorrelationMap<StoredAuthenticationRequest> connectorRequestCorrelationMap) {
        this.connectorRequestCorrelationMap = connectorRequestCorrelationMap;
    }
}
