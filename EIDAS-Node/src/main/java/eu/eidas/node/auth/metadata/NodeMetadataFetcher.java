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
package eu.eidas.node.auth.metadata;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.opensaml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EIDASUtil;
import eu.eidas.auth.engine.metadata.EntityDescriptorContainer;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.impl.AbstractCachingMetadataFetcher;

/**
 * The implementation of the {@link MetadataFetcherI} interface for the Node.
 *
 * @since 1.1
 */
public class NodeMetadataFetcher extends AbstractCachingMetadataFetcher implements IStaticMetadataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(NodeMetadataFetcher.class);

    private boolean httpRetrievalEnabled = false;

    /**
     * when restrictHttp is true, remote metadata is accepted only through https otherwise, metadata retrieved using
     * http protocol is also accepted
     */
    private boolean restrictHttp = false;

    /**
     * whether to enable the signature validation for EntityDescriptors
     */
    private boolean validateEntityDescriptorSignature = true;

    /**
     * initialized with a list of urls corresponding to EntityDescriptor not needing signature validation
     */
    private String trustedEntityDescriptors;

    private NODEFileMetadataProcessor fileMetadataLoader;

    private Set<String> trustedEntityDescriptorsSet = new HashSet<String>();

    private IMetadataCachingService cache;

    @Override
    public void add(EntityDescriptor ed) {
        if (null != cache) {
            cache.putDescriptor(ed.getEntityID(), ed, EntityDescriptorType.STATIC);
        }
    }

    public IMetadataCachingService getCache() {
        return cache;
    }

    public NODEFileMetadataProcessor getFileMetadataLoader() {
        return fileMetadataLoader;
    }

    @Override
    protected EntityDescriptor getFromCache(String url) {
        if (null != cache) {
            EntityDescriptor entityDescriptor = cache.getDescriptor(url);
            if (entityDescriptor != null && !entityDescriptor.isValid()) {
                LOG.error(
                        "Invalid cached metadata information associated with " + url + ", removing it from the cache");
                removeFromCache(url);
            }
        }
        return null;
    }

    public String getTrustedEntityDescriptors() {
        return trustedEntityDescriptors;
    }

    public Set<String> getTrustedEntityDescriptorsSet() {
        return trustedEntityDescriptorsSet;
    }

    /**
     * perform post construct task, eg populating the cache with file based metadata
     */
    public void initProcessor() {
        if (getFileMetadataLoader() != null) {
            List<EntityDescriptorContainer> fileStoredDescriptors = getFileMetadataLoader().getEntityDescriptors();
            if (getCache() != null) {
                for (EntityDescriptorContainer edc : fileStoredDescriptors) {
                    for (EntityDescriptor ed : edc.getEntityDescriptors()) {
                        getCache().putDescriptor(ed.getEntityID(), ed, EntityDescriptorType.STATIC);
                        if (edc.getEntitiesDescriptor() != null && ed.getSignature() == null) {
                            getCache().putDescriptorSignatureHolder(ed.getEntityID(), edc);
                        }
                    }
                }
            }
            getFileMetadataLoader().addListenerContentChanged(this);
        }
    }

    @Override
    public boolean isHttpRetrievalEnabled() {
        return httpRetrievalEnabled;
    }

    public boolean isRestrictHttp() {
        return restrictHttp;
    }

    public boolean isValidateEntityDescriptorSignature() {
        return validateEntityDescriptorSignature;
    }

    @Override
    protected boolean mustUseHttps() {
        return restrictHttp;
    }

    @Override
    protected boolean mustValidateSignature(@Nonnull String url) {
        return validateEntityDescriptorSignature && !trustedEntityDescriptors.contains(url);
    }

    @Override
    protected void putInCache(String url, EntityDescriptor entityDescriptor) {
        if (null != cache && null != entityDescriptor && entityDescriptor.isValid()) {
            cache.putDescriptor(url, entityDescriptor, EntityDescriptorType.DYNAMIC);
        }
    }

    @Override
    public void remove(String entityID) {
        removeFromCache(entityID);
    }

    @Override
    protected void removeFromCache(String url) {
        if (null != cache) {
            cache.putDescriptor(url, null, null);
        }
    }

    public void setCache(IMetadataCachingService cache) {
        this.cache = cache;
    }

    public void setFileMetadataLoader(NODEFileMetadataProcessor fileMetadataLoader) {
        this.fileMetadataLoader = fileMetadataLoader;
    }

    public void setHttpRetrievalEnabled(boolean httpRetrievalEnabled) {
        this.httpRetrievalEnabled = httpRetrievalEnabled;
    }

    public void setRestrictHttp(boolean restrictHttp) {
        this.restrictHttp = restrictHttp;
    }

    public void setTrustedEntityDescriptors(String trustedEntityDescriptors) {
        this.trustedEntityDescriptors = trustedEntityDescriptors;
        setTrustedEntityDescriptorsSet(EIDASUtil.parseSemicolonSeparatedList(trustedEntityDescriptors));
    }

    public void setTrustedEntityDescriptorsSet(Set<String> trustedEntityDescriptorsSet) {
        this.trustedEntityDescriptorsSet = trustedEntityDescriptorsSet;
    }

    public void setValidateEntityDescriptorSignature(boolean validateEntityDescriptorSignature) {
        this.validateEntityDescriptorSignature = validateEntityDescriptorSignature;
    }
}
