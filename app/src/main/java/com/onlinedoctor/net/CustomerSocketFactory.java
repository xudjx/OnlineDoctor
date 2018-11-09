package com.onlinedoctor.net;

/**
 * Created by Administrator on 2015/9/5.
 */
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import org.apache.http.conn.ssl.SSLSocketFactory;

import android.content.Context;
import android.content.res.AssetManager;

import com.onlinedoctor.pojo.Common;

public class CustomerSocketFactory extends SSLSocketFactory {

    public CustomerSocketFactory(KeyStore truststore)
            throws NoSuchAlgorithmException, KeyManagementException,
            KeyStoreException, UnrecoverableKeyException {
        super(truststore);
    }

    public static SSLSocketFactory getSocketFactory(Context context) {
        AssetManager am = context.getAssets();
        InputStream ins = null;
        try {
            if(Common.ENVIRONMENT.equals(Common.PROD_SERVER))
                ins = am.open("prod_server.crt");
            else if(Common.ENVIRONMENT.equals(Common.TEST_SERVER))
                ins = am.open("test_server.crt");
            else
                ins = am.open("prod_server.crt");

            CertificateFactory cerFactory = CertificateFactory.getInstance("X.509");
            Certificate cer = cerFactory.generateCertificate(ins);

            KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
            keyStore.load(null, null);
            keyStore.setCertificateEntry("trust", cer);

            SSLSocketFactory factory = new CustomerSocketFactory(keyStore);

            return factory;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ins = null;
            }
        }
    }

}
