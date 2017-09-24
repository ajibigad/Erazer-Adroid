package com.ajibigad.erazer;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.realm.Realm;

/**
 * Created by ajibigad on 30/07/2017.
 */

public class ErazerApplication extends Application {

    private static final String PREFERENCE_FIRST_RUN = "first_run_key";

    @Override
    public void onCreate() {
        super.onCreate();

        //change this to default initiazer if you want to use sqlite instead of realm
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());

//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        prefs.edit().clear().commit();
//        boolean firstRun = prefs.getBoolean(PREFERENCE_FIRST_RUN, true);
//
//        if(firstRun){
//            try {
//                KeyStoreHelper.setupKeyStore(this);
//                KeyStoreHelper.saveSecretKey(this);
//            } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
//                Log.e(ErazerApplication.class.getSimpleName(), "Failed to setup keystore");
//                e.printStackTrace();
//            } catch (Exception e) {
//                Log.e(ErazerApplication.class.getSimpleName(), "Failed to save secret key");
//                e.printStackTrace();
//            }
//        }
//        prefs.edit().putBoolean(PREFERENCE_FIRST_RUN, false).commit();

        Realm.init(this);

//        Key secretKey = null;
//        try {
//            secretKey = KeyStoreHelper.getSecretKey(this);
//            if(secretKey == null){
//                //generate a new key
//                KeyStoreHelper.saveSecretKey(this);
//                secretKey = KeyStoreHelper.getSecretKey(this);
//            }
//        } catch (Exception e) {
//            Log.e(this.getClass().getSimpleName(), "Failed to get key");
//            e.printStackTrace();
//        }
//
//        byte[] key = secretKey.getEncoded();
//
//        RealmConfiguration config = new RealmConfiguration.Builder()
//                .encryptionKey(key)
//                .build();
//        Realm.setDefaultConfiguration(config);
    }
}
