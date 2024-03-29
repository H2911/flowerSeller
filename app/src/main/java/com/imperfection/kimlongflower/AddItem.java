package com.imperfection.kimlongflower;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AddItem extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ImageView imageView;
    TextView tvNameItem;
    TextView tvUnit;
    ItemsModel itemsModel;
    String action;
    String realQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        imageView = findViewById(R.id.imItem);
        tvNameItem = findViewById(R.id.tvItemName);
        tvUnit = findViewById(R.id.tvUnit);

        Intent intent = getIntent();

        if (intent.getExtras()!=null){
            action  = (String) getIntent().getSerializableExtra("action");
            realQuantity = (String) getIntent().getSerializableExtra("realQuantity");
            itemsModel = (ItemsModel) getIntent().getSerializableExtra("item");
            imageView.setImageResource(itemsModel.getImage());

            //get name, set name
            tvNameItem.setText(itemsModel.getName());

            //get unit, set unit
            tvUnit.setText(itemsModel.getUnit());
        }

        EditText edPrice = findViewById(R.id.edPrice);
        //Separate number by thousand
        edPrice.addTextChangedListener(new NumberTextWatcherForThousand(edPrice));

        EditText edQuantity = findViewById(R.id.edQuantity);
        //Separate number by thousand
        edQuantity.addTextChangedListener(new NumberTextWatcherForThousand(edQuantity));

        //Cancel add item to list process
        ImageButton btnCancel = findViewById(R.id.ibCancel);
        btnCancel.setOnClickListener(v -> finish());

        //Add item to list
        Button bntSubmit = findViewById(R.id.bntSubmit);
        bntSubmit.setOnClickListener(v -> {
            if (edPrice.getText().toString().trim().matches("")) {
                Toast.makeText(AddItem.this, "Đơn giá không thể bỏ trống", Toast.LENGTH_SHORT).show();
            } else if (edQuantity.getText().toString().trim().matches("")) {
                Toast.makeText(AddItem.this, "Số lượng không thể bỏ trống", Toast.LENGTH_SHORT).show();
            } else {
                //remove comma separate thousand
                String quantity = NumberTextWatcherForThousand.trimCommaOfString(edQuantity.getText().toString());
                System.out.println(Integer.parseInt(realQuantity));
                if (action.equals("sell") && (Integer.parseInt(quantity) > Integer.parseInt(realQuantity))) {
                    final AlertDialog.Builder insufficientAlert = new AlertDialog.Builder(AddItem.this);
                    insufficientAlert.setMessage("Số lượng hàng không đủ!!" + "\n" + "Số lượng hàng còn lại: " + NumberTextWatcherForThousand.getDecimalFormattedString(realQuantity));
                    insufficientAlert.setNegativeButton("OK", (dialog, which) -> {
                        //reset quantity
                        edQuantity.setText("");
                    });
                    insufficientAlert.create().show();
                }
                else {
                    Main.productsList.add(new Item(tvNameItem.getText().toString(), NumberTextWatcherForThousand.trimCommaOfString(edQuantity.getText().toString()),NumberTextWatcherForThousand.trimCommaOfString(edPrice.getText().toString()), tvUnit.getText().toString()));
                    ListProduct.activityProduct.finish();
                    CreateInvoice.customAdapter.notifyDataSetChanged();
                    finish();
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onBackPressed(){
        finish();
    }
}