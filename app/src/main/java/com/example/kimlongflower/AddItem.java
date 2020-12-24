package com.example.kimlongflower;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddItem extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ImageView imageView;
    TextView textView;
    ItemsModel itemsModel;
    String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        action  = (String) getIntent().getSerializableExtra("action");
        Spinner spinner = findViewById(R.id.spinUnit);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.unit_arrays, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        imageView = findViewById(R.id.imItem);
        textView = findViewById(R.id.tvItemName);

        Intent intent = getIntent();

        if (intent.getExtras()!=null){
            itemsModel = (ItemsModel) getIntent().getSerializableExtra("item");
            imageView.setImageResource(itemsModel.getImage());
            textView.setText(itemsModel.getName());
        }

        TextView tvItemName = findViewById(R.id.tvItemName);
        EditText edPrice = findViewById(R.id.edPrice);
        EditText edQuantity = findViewById(R.id.edQuantity);

        //Cancel add item to list process
        ImageButton bntCancel = findViewById(R.id.ibCancel);
        bntCancel.setOnClickListener(v -> finish());

        //Add item to list
        Button bntSubmit = findViewById(R.id.bntSubmit);
        bntSubmit.setOnClickListener(v -> {
            if (edPrice.getText().toString().matches("")) {
                Toast.makeText(getApplicationContext(), "Đơn giá không thể bỏ trống", Toast.LENGTH_SHORT).show();
            } else if (edQuantity.getText().toString().matches("")) {
                Toast.makeText(getApplicationContext(), "Số lượng không thể bỏ trống", Toast.LENGTH_SHORT).show();
            } else {
                if (action.equals("sell") && (Integer.parseInt(edQuantity.getText().toString()) > Integer.parseInt(itemsModel.getQuantity().split(" ")[0].trim()))) {
                    final AlertDialog.Builder insufficientAlert = new AlertDialog.Builder(AddItem.this);
                    insufficientAlert.setMessage("Số lượng hàng không đủ!!" + "\n" + "Số lượng hàng còn lại: " + itemsModel.getQuantity());
                    insufficientAlert.setNegativeButton("OK", (dialog, which) -> {
                        //reset quantity
                        edQuantity.setText("");
                    });
                    insufficientAlert.create().show();
                }
                else {
                    Main.productsList.add(new Item(tvItemName.getText().toString(), edQuantity.getText().toString(),edPrice.getText().toString(), spinner.getSelectedItem().toString()));
                    CreateInvoice.activityBuy.recreate();
                    ListProduct.activityProduct.finish();
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