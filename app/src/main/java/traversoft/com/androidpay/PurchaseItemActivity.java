package traversoft.com.androidpay;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentMethodTokenizationType;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.android.gms.wallet.fragment.WalletFragmentStyle;


public class PurchaseItemActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "PurchaseItemActivity";
    private SupportWalletFragment walletFragment;
    private MaskedWallet maskedWallet;
    private FullWallet fullWallet;
    private GoogleApiClient googleApiClient;

    public static final int MASKED_WALLET_REQUEST_CODE = 1000;

    public static final int FULL_WALLET_REQUEST_CODE = 889;

    private static final String WALLET_FRAGMENT = "WALLET_FRAGMENT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_item);

        this.googleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .enableAutoManage(this, 0, this)
                .addApi(Wallet.API, new Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                        .setTheme(WalletConstants.THEME_LIGHT)
                        .build())
                .build();

        this.walletFragment =
                (SupportWalletFragment) getSupportFragmentManager().findFragmentByTag(WALLET_FRAGMENT);

        if (this.walletFragment == null) {

            WalletFragmentStyle walletFragmentStyle = new WalletFragmentStyle()
                    .setBuyButtonText(WalletFragmentStyle.BuyButtonText.BUY_WITH)
                    .setBuyButtonWidth(WalletFragmentStyle.Dimension.MATCH_PARENT);

            WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
                    .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                    .setFragmentStyle(walletFragmentStyle)
                    .setTheme(WalletConstants.THEME_LIGHT)
                    .setMode(WalletFragmentMode.BUY_BUTTON)
                    .build();

            WalletFragmentInitParams.Builder startParamsBuilder =
                    WalletFragmentInitParams.newBuilder()
                            .setMaskedWalletRequest(generateMaskedWalletRequest())
                            .setMaskedWalletRequestCode(MASKED_WALLET_REQUEST_CODE)
                            .setAccountName("Mike's Pies");
            this.walletFragment = SupportWalletFragment.newInstance(walletFragmentOptions);
            this.walletFragment.initialize(startParamsBuilder.build());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.wallet_button_holder, this.walletFragment, WALLET_FRAGMENT)
                    .commit();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MASKED_WALLET_REQUEST_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        this.maskedWallet =  data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                        Toast.makeText(this, "Got Masked Wallet", Toast.LENGTH_SHORT).show();
                        break;
                    case RESULT_CANCELED:
                        // The user canceled the operation
                        break;
                    case WalletConstants.RESULT_ERROR:
                        Toast.makeText(this, "An Error Occurred", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            case FULL_WALLET_REQUEST_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        this.fullWallet = data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);
                        Toast.makeText(this, "Got Full Wallet, Done!", Toast.LENGTH_SHORT).show();
                        break;
                    case WalletConstants.RESULT_ERROR:
                        Toast.makeText(this, "An Error Occurred", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
        }
    }

    public void requestFullWallet(View view) {

        if (this.maskedWallet == null) {
            Toast.makeText(this, "No masked wallet, can't confirm", Toast.LENGTH_SHORT).show();
            return;
        }
        Wallet.Payments.loadFullWallet(this.googleApiClient,
                generateFullWalletRequest(this.maskedWallet.getGoogleTransactionId()),
                FULL_WALLET_REQUEST_CODE);
    }

    private MaskedWalletRequest generateMaskedWalletRequest() {
        // This is just an example publicKey.
        // To learn how to generate your own visit:
        // https://github.com/android-pay/androidpay-quickstart
        String publicKey = "BO39Rh43UGXMQy5PAWWe7UGWd2a9YRjNLPEEVe+zWIbdIgALcDcnYCuHbmrrzl7h8FZjl6RCzoi5/cDrqXNRVSo=";
        PaymentMethodTokenizationParameters parameters =
                PaymentMethodTokenizationParameters.newBuilder()
                        .setPaymentMethodTokenizationType(
                                PaymentMethodTokenizationType.NETWORK_TOKEN)
                        .addParameter("publicKey", publicKey)
                        .build();

        MaskedWalletRequest maskedWalletRequest =
                MaskedWalletRequest.newBuilder()
                        .setMerchantName("Mike's Pies")
                        .setPhoneNumberRequired(true)
                        .setShippingAddressRequired(true)
                        .setCurrencyCode("USD")
                        .setCart(Cart.newBuilder()
                                .setCurrencyCode("USD")
                                .setTotalPrice("12.00")
                                .addLineItem(LineItem.newBuilder()
                                        .setCurrencyCode("USD")
                                        .setDescription("Pumpkin Pie")
                                        .setQuantity("1")
                                        .setUnitPrice("12.00")
                                        .setTotalPrice("13.50")
                                        .build())
                                .build())
                        .setEstimatedTotalPrice("13.50")
                        .setPaymentMethodTokenizationParameters(parameters)
                        .build();
        return maskedWalletRequest;
    }

    private FullWalletRequest generateFullWalletRequest(String googleTransactionId) {

        FullWalletRequest fullWalletRequest = FullWalletRequest.newBuilder()
                .setGoogleTransactionId(googleTransactionId)
                .setCart(Cart.newBuilder()
                        .setCurrencyCode("USD")
                        .setTotalPrice("13.50")
                        .addLineItem(LineItem.newBuilder()
                                .setCurrencyCode("USD")
                                .setDescription("Pumpkin Pie")
                                .setQuantity("1")
                                .setUnitPrice("12.00")
                                .setTotalPrice("12.00")
                                .build())
                        .addLineItem(LineItem.newBuilder()
                                .setCurrencyCode("USD")
                                .setDescription("Tax")
                                .setRole(LineItem.Role.TAX)
                                .setTotalPrice("1.50")
                                .build())
                        .build())
                .build();

        return fullWalletRequest;
    }

    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, connectionResult.getErrorCode() + ": " + connectionResult.getErrorMessage());
    }
}
