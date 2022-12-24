package com.wl4g.infra.common.graalvm.feature;

import java.security.Security;

import org.graalvm.nativeimage.hosted.Feature;

import com.oracle.svm.core.annotate.AutomaticFeature;

import net.i2p.crypto.eddsa.EdDSASecurityProvider;

/**
 * {@link EdDSAFeature}
 * 
 * @author James Wong
 * @version 2022-12-25
 * @since https://github.com/oracle/graal/issues/2800
 */
@AutomaticFeature
public class EdDSAFeature implements Feature {

    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        Security.addProvider(new EdDSASecurityProvider());
    }

}