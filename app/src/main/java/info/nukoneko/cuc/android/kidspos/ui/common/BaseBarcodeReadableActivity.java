package info.nukoneko.cuc.android.kidspos.ui.common;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;

public abstract class BaseBarcodeReadableActivity extends BaseActivity {
    private String mInputValue = "";
    private boolean mFlip = false;

    public abstract void onInputBarcode(@NonNull String barcode, BARCODE_TYPE type);

    public enum BARCODE_TYPE {
        STAFF("1000"),
        ITEM("1001"),
        SALE_INFO("1002"),
        UNKNOWN("9999");

        private final String prefix;
        BARCODE_TYPE(@NonNull final String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }

        public static BARCODE_TYPE typeOf(String barcode) {
            if (TextUtils.isEmpty(barcode)) return BARCODE_TYPE.UNKNOWN;
            for (BARCODE_TYPE barcodeType : BARCODE_TYPE.values()) {
                if (barcode.startsWith(barcodeType.getPrefix())) return barcodeType;
            }
            return BARCODE_TYPE.UNKNOWN;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            if (TextUtils.isEmpty(mInputValue)) return false;
            if (mInputValue.length() == 10) {
                onInputBarcode(mInputValue, BARCODE_TYPE.typeOf(mInputValue));
            } else if (getApp().isPracticeModeEnabled() || getApp().isTestModeEnabled()) {
                onInputBarcode(mInputValue, BARCODE_TYPE.typeOf(mInputValue));
            }
            mInputValue = "";
            return false;
        }

        if (mFlip) {
            mInputValue += String.valueOf(event.getKeyCode() - 7);
            mFlip = false;
        } else {
            mFlip = true;
        }
        return super.dispatchKeyEvent(event);
    }
}
