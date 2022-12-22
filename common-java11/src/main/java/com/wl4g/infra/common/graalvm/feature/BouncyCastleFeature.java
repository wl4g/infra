package com.wl4g.infra.common.graalvm.feature;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.impl.RuntimeClassInitializationSupport;

import com.oracle.svm.core.annotate.AutomaticFeature;

/**
 * {@link BouncyCastleFeature}
 * 
 * @author James Wong
 * @version 2022-12-25
 * @since https://github.com/oracle/graal/issues/2800
 */
@AutomaticFeature
public class BouncyCastleFeature implements Feature {

    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        // https://www.graalvm.org/22.0/reference-manual/native-image/JCASecurityServices/
        // Provider bcProvider = Security.getProvider("BC");
        // Security.removeProvider("BC");
        // Security.insertProviderAt(bcProvider, 1);

        RuntimeClassInitialization.initializeAtBuildTime("org.bouncycastle");
        RuntimeClassInitializationSupport rci = ImageSingletons.lookup(RuntimeClassInitializationSupport.class);
        rci.rerunInitialization("org.bouncycastle.jcajce.provider.drbg.DRBG$Default", "");
        rci.rerunInitialization("org.bouncycastle.jcajce.provider.drbg.DRBG$NonceAndIV", "");
        Security.addProvider(new BouncyCastleProvider());
    }

}