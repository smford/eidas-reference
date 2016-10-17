package eu.eidas.idp;

import eu.eidas.auth.commons.EIDASUtil;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.impl.DefaultMetadataFetcher;
import eu.eidas.config.impl.EnvironmentVariableSubstitutor;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.opensaml.saml2.metadata.EntityDescriptor;

import javax.annotation.Nonnull;
import java.util.Properties;

/**
 * IdpMetadataFetcher
 *
 * @since 1.1
 */
public final class IdpMetadataFetcher extends DefaultMetadataFetcher {

    private final Properties idpProperties = new EnvironmentVariableSubstitutor().mutatePropertiesReplaceValues(EIDASUtil.loadConfigs(Constants.IDP_PROPERTIES));

    @Nonnull
    @Override
    public EntityDescriptor getEntityDescriptor(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner)
            throws EIDASSAMLEngineException {
        boolean checkMetadata = idpProperties != null && Boolean.parseBoolean(idpProperties.getProperty(IDPUtil.ACTIVE_METADATA_CHECK));
        if (checkMetadata) {
            return super.getEntityDescriptor(url, metadataSigner);
        }
        return null;
    }

    @Override
    protected boolean mustUseHttps() {
        return false;
    }

    @Override
    protected boolean mustValidateSignature(@Nonnull String url) {
        return false;
    }
}
