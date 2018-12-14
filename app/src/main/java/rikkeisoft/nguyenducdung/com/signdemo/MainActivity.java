package rikkeisoft.nguyenducdung.com.signdemo;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText etSign;
    private Button btnSign;
    private Dialog dialog;
    private SignatureView mSignView;
    private List<Bitmap> bitmaps = new ArrayList<>();
    private List<File> uploadFiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });


    }

    private void init() {
        etSign = findViewById(R.id.et_sign);
        btnSign = findViewById(R.id.btn_sign);
    }

    public void showDialog() {
        dialog = new Dialog(MainActivity.this);
        dialog.setTitle("Sign");
        dialog.setContentView(R.layout.sign_dialog);
        mSignView = dialog.findViewById(R.id.sign);
        mSignView.setSignatureCallBack(new SignatureView.ISignatureCallBack() {
            @Override
            public void onSignCompeleted(View view, Bitmap bitmap) {
                String fileDir = getExternalCacheDir() + "signature/";
                String path = fileDir + SystemClock.elapsedRealtime() + ".png";
                File file=new File(fileDir);
                if(!file.exists()){
                    file.mkdir();
                }
                bitmaps.add(bitmap);
                try {
                    mSignView.save(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                uploadFiles.add(new File(path));
                drawBitmaps(bitmap);
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void drawBitmaps(Bitmap b) {
        ImageSpan imgSpan = new ImageSpan(this, b);
        SpannableString spanString = new SpannableString("i");
        spanString.setSpan(imgSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        etSign.append(spanString);
    }
}
