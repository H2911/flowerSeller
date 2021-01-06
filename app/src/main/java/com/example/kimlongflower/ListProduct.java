package com.example.kimlongflower;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListProduct extends AppCompatActivity {

    final int[] images = {R.drawable.cucchumcanhdai,R.drawable.at,R.drawable.cucchumtrang,R.drawable.cucchummaicam,R.drawable.cucchummaido,R.drawable.cucchummaitim,
    R.drawable.cucchummaihong,R.drawable.cucchummaivang,0,R.drawable.cucchumcalimerodumau,R.drawable.cucchumfarmtim,R.drawable.cucchumfarmvang,R.drawable.cucchumfarmtrang,
    R.drawable.cucchumthachbichdam,R.drawable.cucchumthachbichnhat,R.drawable.cucchumnhinau,R.drawable.cucchummonatrang,R.drawable.cucchummonavang,R.drawable.cucchumtaichuot,
    R.drawable.cucchumnhimxanh,0,0,R.drawable.cucmotluoidoacu,R.drawable.cucmotluoidoamoi,R.drawable.cucmotluoixanh,R.drawable.cucmotluoitrang,
    R.drawable.cucmotluoinhuommau,0,0,0,0,0,R.drawable.cucmotkimcuong,R.drawable.cucmotkimcuongtrung,R.drawable.cucmotchensaphia,R.drawable.cucmotpingpong,
    R.drawable.cucmotchendoamoi,R.drawable.cucmotluoitrung,R.drawable.cucmotbivang,R.drawable.cucmotbitrang,R.drawable.cattuong,R.drawable.cattuong,
    R.drawable.huongduong,R.drawable.huongduong,R.drawable.tucau,R.drawable.camchuong,R.drawable.camchuong,R.drawable.dongtien,R.drawable.dongtienquan,
    R.drawable.dondo,R.drawable.donvang,R.drawable.donhong,R.drawable.dontrung,R.drawable.lanvitrang,R.drawable.lanvitim,R.drawable.lanvivang,R.drawable.momsoi,
    R.drawable.momcacloai,R.drawable.momcacloai,R.drawable.hoahongdumau,R.drawable.hoahongsenmoi,R.drawable.hoahongsen,R.drawable.hoahongsendai,R.drawable.hoahongcamdai,
    R.drawable.hoahonganhtrang,R.drawable.hoahongdosa,R.drawable.hoahongdohalan,R.drawable.hoahongdoot,R.drawable.hoahongphan,R.drawable.hoahonghoanghau,R.drawable.hoahongmoga,
    R.drawable.hoahongkem,R.drawable.hoahongtrangxoay,R.drawable.hoahongxanhngoc,R.drawable.hoahonghy,R.drawable.hoahonghyxanh};
    final String[] names = {"Cúc chùm cánh dài","AT","Cúc chùm trắng","Cúc chùm mai cam","Cúc chùm mai đỏ","Cúc chùm mai tím","Cúc chùm mai hồng","Cúc chùm mai vàng",
            "Cúc chùm lan tím","Cúc chùm Calimero (Kaly) đủ màu","Cúc chùm farm tím","Cúc chùm farm vàng","Cúc chùm farm trắng","Cúc chùm thạch bích đậm",
            "Cúc chùm thạch bích nhạt","Cúc chùm nhị nâu","Cúc chùm Mona trắng","Cúc chùm Mona vàng","Cúc chùm tai chuột","Cúc chùm nhím xanh","Cúc chùm trung",
            "Cúc chùm xả","Cúc một lưới đóa cũ","Cúc một lưới đóa mới","Cúc một lưới xanh","Cúc một lưới trắng","Cúc một lưới nhuộm màu","Cúc một lưới nhuộm màu đỏ",
            "Cúc một lưới nhuộm màu cam","Cúc một lưới nhuộm màu xanh đậm","Cúc một lưới nhuộm màu xanh lá","Cúc một lưới nhuộm màu tím","Cúc một kim cương","Cúc một kim cương trung",
            "Cúc một chén Saphia","Cúc một ping pong","Cúc một chén đóa mới","Cúc một lưới trung","Cúc một bi vàng","Cúc một bi trắng","Cát tường (bó)","Cát tường (kg)","Hướng dương (cành)","Hướng dương (bó)","Tú cầu",
            "Cẩm chướng (cành)","Cẩm chướng (bó)","Đồng tiền không quấn","Đồng tiền quấn","Dơn đỏ","Dơn vàng","Dơn hồng","Dơn trung","Lan vỉ trắng","Lan vỉ tím","Lan vỉ vàng","Mõm sói","Môn các loại (cành)","Môn các loại (bó)","Hoa hồng đủ màu",
            "Hoa hồng sen mới","Hoa hồng sen","Hoa hồng sen đại","Hoa hồng cam dài","Hoa hồng ánh trăng","Hoa hồng đỏ sa","Hoa hồng đỏ Hà Lan","Hoa hồng đỏ ớt","Hoa hồng phấn","Hoa hồng hoàng hậu",
            "Hoa hồng mỡ gà","Hoa hồng kem","Hoa hồng trắng xoáy","Hoa hồng xanh ngọc","Hoa hồng hỷ","Hoa hồng hỷ xanh","Hoa hồng hỷ hồng","Hoa hồng tím","Hoa hồng tím cồ","Hoa hồng nhỏ","Hoa hồng vàng chùa",
            "Hoa hồng sen phật","Hoa hồng mật ong","Hoa hồng Hara","Hoa hồng son môi","Hoa hồng trà","Ly nhọn 1 tai","Ly nhọn 2 tai","Ly nhọn 3 tai","Ly nhọn 4 tai","Ly nhọn 5 tai","Ly ù đỏ 1 tai","Ly ù đỏ 2 tai",
            "Ly ù đỏ 3 tai","Ly ù đỏ 4 tai","Ly ù đỏ 5 tai","Ly ù vàng 1 tai","Ly ù vàng 2 tai","Ly ù vàng 3 tai","Ly ù vàng 4 tai","Ly ù vàng 5 tai","Dừa nhỏ","Dừa lớn","Lá lan","Lá mimosa",
            "Lá nguyệt quế","Lá đốm","Lá đô la","Thạch thảo tím (kg)","Thạch thảo tím (bó)","Thạch thảo trắng (kg)","Thạch thảo trắng (bó)","Vàng anh (kg)","Vàng anh (bó)","Sa lem (kg)","Sa lem (bó)","Sao tím (kg)","Sao tím (bó)","Bibi","Bibi ngoại","Mimi"};
    public static Activity activityProduct;
    List<ItemsModel> itemsModelList = new ArrayList<>();
    GridView gvProduct;
    CustomAdapter customAdapter;//custom grid view product
    SearchView searchView;
    String action;
    FirebaseUser user;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_product);

        activityProduct = this;
        action = (String) getIntent().getSerializableExtra("action");

        user = FirebaseAuth.getInstance().getCurrentUser();
        for(int i = 0;i<names.length;i++) {
            int j = i;
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("storage").child(names[i]);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(images.length>j&&images[j]!=0) {
                        itemsModelList.add(new ItemsModel(names[j], snapshot.getValue().toString().split(" ")[0].trim(),snapshot.getValue().toString().split(" ")[1].trim(), images[j]));
                    }else {
                        itemsModelList.add(new ItemsModel(names[j],snapshot.getValue().toString().split(" ")[0].trim(),snapshot.getValue().toString().split(" ")[1].trim(),R.drawable.logo));
                    }
                    customAdapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        gvProduct = findViewById(R.id.gvProduct);
        customAdapter = new CustomAdapter(itemsModelList,this);
        gvProduct.setAdapter(customAdapter);

        //Back to buy page
        Button btnBackBuyPage = findViewById(R.id.bntBackBuyPage);
        btnBackBuyPage.setOnClickListener(v -> finish());

        //Search product base on the name
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                customAdapter.getFilter().filter(newText);
                return true;
            }
        });

    }

    public class CustomAdapter extends BaseAdapter implements Filterable {
        private List<ItemsModel> itemsModelList;
        private List<ItemsModel> itemsModelListFiltered;
        private Context context;

        public CustomAdapter(List<ItemsModel> itemsModelList,Context context){
            this.itemsModelList = itemsModelList;
            this.context = context;
            this.itemsModelListFiltered = itemsModelList;
        }

        @Override
        public int getCount() {
            return itemsModelListFiltered.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.row_items,null);
            ImageView imageView = view.findViewById(R.id.ivItem);
            TextView tvName = view.findViewById(R.id.tvItem);
            TextView tvQuantity = view.findViewById(R.id.tvQuantity);

            imageView.setImageResource(itemsModelListFiltered.get(position).getImage());
            tvName.setText(itemsModelListFiltered.get(position).getName());
            tvQuantity.setText(itemsModelListFiltered.get(position).getQuantity() +" "+ itemsModelListFiltered.get(position).getUnit());

            view.setOnClickListener(v -> {
                if(!action.equals("view store")) {
                    Intent intent = new Intent(ListProduct.this, AddItem.class).putExtra("item", itemsModelListFiltered.get(position));
                    intent.putExtra("action", action);
                    startActivity(intent);
                }
            });

            return view;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();

                    if(constraint==null||constraint.length()==0){
                        filterResults.count = itemsModelList.size();
                        filterResults.values = itemsModelList;
                    }else{
                        String searchStr = constraint.toString().toLowerCase();
                        List<ItemsModel> rsData = new ArrayList<>();

                        for(ItemsModel itemsModel:itemsModelList){
                            if(itemsModel.getName().toLowerCase().contains(searchStr.toLowerCase())
                                    ||VNCharacterUtils.removeAccent(itemsModel.getName().toLowerCase()).contains(VNCharacterUtils.removeAccent(searchStr.toLowerCase()))){
                                rsData.add(itemsModel);
                            }
                            filterResults.count = rsData.size();
                            filterResults.values = rsData;
                        }
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    itemsModelListFiltered = (List<ItemsModel>) results.values;
                    notifyDataSetChanged();
                }
            };
        }
    }

    public void onBackPressed(){ finish();
    }
}