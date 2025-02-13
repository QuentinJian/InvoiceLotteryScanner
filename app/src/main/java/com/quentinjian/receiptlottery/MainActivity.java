package com.quentinjian.receiptlottery;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static String[] prices;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);
        TextView displaySpecialPrice = findViewById(R.id.display_special_price);
        TextView displayGrandPrice = findViewById(R.id.display_grand_number);
        TextView displayPrizes1 = findViewById(R.id.display_prizes1);
        TextView displayPrizes2 = findViewById(R.id.display_prizes2);
        TextView displayPrizes3 = findViewById(R.id.display_prizes3);
        Thread thread = new Thread(() -> {
            try {
                prices = RetrieveDataFromNet.retrieveCode(RetrieveDataFromNet.InvoiceNumberUrl);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displaySpecialPrice.setText(prices[0]);
                        displayGrandPrice.setText(prices[1]);
                        SpannableStringBuilder sb1 = new SpannableStringBuilder(prices[2]);
                        sb1.setSpan(new ForegroundColorSpan(Color.rgb(220, 54, 72)),
                                5, 8, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        displayPrizes1.setText(sb1, TextView.BufferType.SPANNABLE);
                        SpannableStringBuilder sb2 = new SpannableStringBuilder(prices[3]);
                        sb2.setSpan(new ForegroundColorSpan(Color.rgb(220, 54, 72)),
                                5, 8, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        displayPrizes2.setText(sb2, TextView.BufferType.SPANNABLE);
                        SpannableStringBuilder sb3 = new SpannableStringBuilder(prices[4]);
                        sb3.setSpan(new ForegroundColorSpan(Color.rgb(220, 54, 72)),
                                5, 8, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        displayPrizes3.setText(sb3, TextView.BufferType.SPANNABLE);
                    }
                });
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, "Data retrieve failed !!!",
                        Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Exception occurred: " + e);
                throw new RuntimeException(e);
            }
        });
        thread.start();
    }


    public void onClickOpenCamera(View view) {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivity(intent);
    }

    public void onClickShowRule(View view) {
        ShowRuleDialog showRuleDialog = new ShowRuleDialog();
        showRuleDialog.show(getSupportFragmentManager(), "RULE_DIALOG");
    }
}
