package com.example.kimlongflower;

import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.data.printable.Printable;
import com.mazenrashed.printooth.data.printable.TextPrintable;
import com.mazenrashed.printooth.data.printer.DefaultPrinter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Invoice {
    private final String date;
    private final String time;
    private final String name;
    private List<Item> listItem;
    private final String action;
    private int sum;

    public Invoice(String name, String action, List<Item> listItem){
        Date date = new Date();
        SimpleDateFormat formatterDate = new SimpleDateFormat("dd_MM_yyyy");
        SimpleDateFormat formatterTime = new SimpleDateFormat("hh:mm:ss");
        this.date = formatterDate.format(date);
        this.time = formatterTime.format(date);
        this.name = name;
        this.listItem = listItem;
        this.action = action;

    }

    public Invoice(String name, String action, List<Item> listItem, String date,String time){
        this.date = date;
        this.time = time;
        this.name = name;
        this.listItem = listItem;
        this.action = action;
    }

    public int getSum() {
        for (Item item : listItem) {
            sum +=Integer.parseInt(item.getSumOfItem());
        }
        return sum;
    }

    public void printInvoice(){


        ArrayList<Printable> printable = new ArrayList<>();

        printable.add(new TextPrintable.Builder()
                .setText("Công ty TNHH Kim Long Flower")
                .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_PC1252())
                .setNewLinesAfter(2)
                .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                .setFontSize(DefaultPrinter.Companion.getFONT_SIZE_NORMAL())
                .setEmphasizedMode(DefaultPrinter.Companion.getEMPHASIZED_MODE_BOLD())
                .build());

        printable.add(new TextPrintable.Builder()
                .setText("12b Nguyễn Du, Phường 9, Đà Lạt, Lâm Đồng")
                .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_PC1252())
                .setNewLinesAfter(2)
                .setFontSize(DefaultPrinter.Companion.getFONT_SIZE_NORMAL())
                .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                .build());

        printable.add(new TextPrintable.Builder()
                .setText("SĐT: 0913789317")
                .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_WEU())
                .setNewLinesAfter(1)
                .setFontSize(DefaultPrinter.Companion.getFONT_SIZE_NORMAL())
                .setAlignment(DefaultPrinter.Companion.getALIGNMENT_RIGHT())
                .build());

        printable.add(new TextPrintable.Builder()
                .setText("SĐT: 0982789317")
                .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_PC1252())
                .setNewLinesAfter(3)
                .setFontSize(DefaultPrinter.Companion.getFONT_SIZE_NORMAL())
                .setAlignment(DefaultPrinter.Companion.getALIGNMENT_RIGHT())
                .build());

        if(action.equals("buy")){
            printable.add(new TextPrintable.Builder()
                    .setText("HÓA ĐƠN MUA HÀNG")
                    .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_PC1252())
                    .setNewLinesAfter(3)
                    .setFontSize(DefaultPrinter.Companion.getFONT_SIZE_LARGE())
                    .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                    .setEmphasizedMode(DefaultPrinter.Companion.getEMPHASIZED_MODE_BOLD())
                    .build());

            printable.add(new TextPrintable.Builder()
                    .setText("Người bán: "+name)
                    .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_PC1252())
                    .setAlignment(DefaultPrinter.Companion.getALIGNMENT_LEFT())
                    .setNewLinesAfter(3)
                    .setUnderlined(DefaultPrinter.Companion.getUNDERLINED_MODE_ON())
                    .build());
        }else {
            printable.add(new TextPrintable.Builder()
                    .setText("HÓA ĐƠN BÁN HÀNG")
                    .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_PC1252())
                    .setNewLinesAfter(3)
                    .setFontSize(DefaultPrinter.Companion.getFONT_SIZE_LARGE())
                    .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                    .setEmphasizedMode(DefaultPrinter.Companion.getEMPHASIZED_MODE_BOLD())
                    .build());

            printable.add(new TextPrintable.Builder()
                    .setText("Người mua: "+name)
                    .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_PC1252())
                    .setAlignment(DefaultPrinter.Companion.getALIGNMENT_LEFT())
                    .setNewLinesAfter(3)
                    .setUnderlined(DefaultPrinter.Companion.getUNDERLINED_MODE_ON())
                    .build());
        }



        int order = 1;
        for (Item item : listItem){
            printable.add(new TextPrintable.Builder()
                    .setText(order +". " + item.getName())
                    .setNewLinesAfter(1)
                    .setAlignment(DefaultPrinter.Companion.getALIGNMENT_LEFT())
                    .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_PC1252())
                    .build());

            printable.add(new TextPrintable.Builder()
                    .setText(item.getQuantity() +" "+ item.getUnit() + " x " + item.getPrice() + " = ")
                    .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                    .build());

            printable.add(new TextPrintable.Builder()
                    .setText(item.getSumOfItem() + " đ")
                    .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_PC1252())
                    .setAlignment(DefaultPrinter.Companion.getALIGNMENT_RIGHT())
                    .setNewLinesAfter(2)
                    .build());
            order++;
        }

        printable.add(new TextPrintable.Builder()
                .setText("----------------")
                .setNewLinesAfter(2)
                .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                .build());

        String sum = String.valueOf(this.sum);
        printable.add(new TextPrintable.Builder()
                .setText("Tổng: "+sum +" đ")
                .setNewLinesAfter(3)
                .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_PC1252())
                .setAlignment(DefaultPrinter.Companion.getALIGNMENT_RIGHT())
                .build());

        printable.add(new TextPrintable.Builder()
                .setText(date+"  "+time)
                .setNewLinesAfter(3)
                .setAlignment(DefaultPrinter.Companion.getALIGNMENT_RIGHT())
                .build());

        Printooth.INSTANCE.printer().print(printable);
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public List<Item> getListItem() {
        return listItem;
    }

    public String getName() {
        return name;
    }
}
