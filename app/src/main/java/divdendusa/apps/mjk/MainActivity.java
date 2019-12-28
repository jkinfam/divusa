package divdendusa.apps.mjk;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private String htmlPageUrl;
    private String htmlPageUrlFull = "https://www.dividend.com/dividend-stocks/#tm=3-top-100&r=Webpage%231282&only=meta%2Cdata";    //jsoup
    private String pageUrl = "&page="; //페이지소스 2538행 부터 관련 테이블 시작임
    private int pageNum=1;

    Cursor c;
    public final static String DB_NAME = "dividendUSA.db";      //DB파일명 설정
    public final static String T_NAME = "DIVIDENDUSA";      //tabled이름 설정
    SQLiteDatabase db;
    MySQLiteOpenHelper helper;
    int tableRow;
    int forNum = 0;
    Object bindArgs[]=new Object[12]; //주식이름부터 그 정보 마지막까지 정보(컬럼)의 개수
    int sqlInt;
    double sqlDouble;
    String sqlNull=null;
    ArrayList<Item> items = new ArrayList();        //recyclerview에 넣을 database값이 들어가야함


    boolean pageCheck=true;             //jsoup
    TextView textviewHtmlDocument;      //jsoup
    String htmlContentInStringFormat = "";       //jsoup
    JsoupAsyncTask jsoupAsyncTask;      //jsoup

    RelativeLayout mRelative;
    RecyclerView mRecyclerView;         //recyclerview
    LinearLayoutManager mLayoutManager;     //recyclerview
    RecyclerViewAdapter mAdapter;       //recyclerview
    Button htmlTitleButton;

    EditText editText;
    String search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //종목검색
        editText = (EditText) findViewById(R.id.EditText01);
        editTextM();

        //jsoup
        htmlTitleButton = (Button)findViewById(R.id.Button01);
        htmlTitleButton.setOnClickListener(new View.OnClickListener(){      //클릭시 수행됨
            @Override
            public void onClick(View v){
                if(db==null) {
                    showMsg(Integer.toString(R.string.gatheringInformation),1);
                    deleteExistingDBFile();
                    createDb();     //버튼을 누르면 DB가 생성됨

                }else{
                    showMsg(Integer.toString(R.string.gatheringInformation),1);
                    //Log.i("한번실행후 재실행","한번실행후 재실행!!!!!!!!!!!!!!!!!!!!!");
                    closeDb();      //한번 실행한 DB가 있으면 테이블과 DB를 없애고
                    deleteExistingDBFile();
                    createDb();     //다시 DB및 테이블을 생성한다
                    items.clear();
                    pageCheck=true;
                    pageNum=1;
                    htmlPageUrl="";
                    //switchUrlKind(kindOf);
                }
                Log.i("mainActivity","JsoupAsyncTask 객체생성전!!");
                jsoupAsyncTask = new JsoupAsyncTask();       // 버튼을 누르면 JSOUP이 실행됨
                Log.i("Jsoup 실행시","jsoupAsyncTask이 NULL 인지??"+jsoupAsyncTask);
                jsoupAsyncTask.execute();       //JSOUP 실행됨
                Log.i("mainActivity","JSOUP 실행완료!!");
            }
        });

        //recyclerview관련
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview);     //recyclerview mRecycleView변수에 recyclerview뷰를 가져옴
        mLayoutManager = new LinearLayoutManager(this);     //recyclerview  mLayoutManager변수에 이 자체를 넣은 LinearLayoutManager객체생성함
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);        //recyclerview  mLayoutManager에 orientation은 vertical로 설정
        mRelative = (RelativeLayout)findViewById(R.id.RelativeLayout01);

        // LinearLayout으로 설정
        mRecyclerView.setLayoutManager(mLayoutManager);
        // Animation Defualt 설정
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        //DB open helper
        helper = new MySQLiteOpenHelper(MainActivity.this, // 현재 화면의 context
                "dividend.db", // 파일명
                null, // 커서 팩토리
                1); // 버전 번호
        /*
        File dbFile = new File("/data/data/apps.mjk.dividend/databases/dividend.db");
        if(dbFile.exists()){
            dbFile.delete();
            //Log.i("기존DB파일","기존DB파일 삭제함");
        }
        */
        createDb();
        select();
        mAdapter = new RecyclerViewAdapter(items);
        mRecyclerView.setAdapter(mAdapter);

    }
    //종목선택관련
    void editTextM(){
        editText.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if((event.getAction())==(KeyEvent.ACTION_DOWN)&&(keyCode==KeyEvent.KEYCODE_ENTER)){
                    search = editText.getText().toString();
                    File dbFile = new File("/data/data/divdendusa.apps.mjk/databases/dividendUSA.db");
                    if(!dbFile.exists()){
                        showMsg(Integer.toString(R.string.get_information_first), 0);
                        editText.setText("");
                    }else {
                        items.clear();
                        c = db.query("DIVIDENDUSA", null, "NAME like '%" + search + "%'", null, null, null, "_SEQ");
                        items.add(new Item(Integer.toString(R.string.seq),
                                                Integer.toString(R.string.stockSymbol),
                                                Integer.toString(R.string.companyName),
                                                Integer.toString(R.string.dividendYield),
                                                Integer.toString(R.string.closingPrice),
                                                Integer.toString(R.string.annualizedDividend),
                                                Integer.toString(R.string.exdivDate),
                                                Integer.toString(R.string.payDate)
                                ));
                        while (c.moveToNext()) {
                            // c의 int가져와라 ( c의 컬럼 중 id) 인 것의 형태이다.
                            int priceInt=nullCheckForInt(c,"PRICE");
                            int annualDividendInt=nullCheckForInt(c,"DIVIDEND");
                            /*int dividendInt=nullCheckForInt(c,"DIVIDEND");
                            int oneInt=nullCheckForInt(c,"ONE");
                            int twoInt=nullCheckForInt(c,"TWO");
                            int threeInt=nullCheckForInt(c,"THREE");*/
                            double yieldDouble = nullCheckForDouble(c,"YIELD");
                            /*double dpratioDouble = nullCheckForDouble(c,"DPRATIO");
                            double roeDouble = nullCheckForDouble(c,"ROE");
                            double perDouble = nullCheckForDouble(c,"PER");
                            double pbrDouble = nullCheckForDouble(c,"PBR");*/

                            String seq = c.getString(c.getColumnIndex("_SEQ"));
                            String symbol = c.getString((c.getColumnIndex("SYMBOL")));
                            String name = c.getString(c.getColumnIndex("NAME"));
                            String yield = nullCheckForDoubleSecond(yieldDouble);
                            String price = nullCheckForIntSecond(priceInt);
                            String dividend = nullCheckForIntSecond(annualDividendInt);
                            String exdivDate = c.getString(c.getColumnIndex("EXDIVDATE"));
                            String payDate = c.getString(c.getColumnIndex("PAYDATE"));
                            /*String dividend = nullCheckForIntSecond(dividendInt);
                            String dpratio = nullCheckForDoubleSecond(dpratioDouble);
                            String roe = nullCheckForDoubleSecond(roeDouble);
                            String per = nullCheckForDoubleSecond(perDouble);
                            String pbr = nullCheckForDoubleSecond(pbrDouble);
                            String one = nullCheckForIntSecond(oneInt);
                            String two= nullCheckForIntSecond(twoInt);
                            String three= nullCheckForIntSecond(threeInt);*/
                            items.add(new Item(seq, symbol, name, yield, price, dividend, exdivDate,payDate));
                            //Log.i("Cursor로 가져오기", "while문에서 items객체 생성!!!! ");
                        }
                        mAdapter = new RecyclerViewAdapter(items);
                        mRecyclerView.setAdapter(mAdapter);
                    }
                    return true;
                }
                return false;
            }
        });
    }
    void showMsg(String msg, int option){
        Toast.makeText(this, msg , option).show();
    }
    public int nullCheckForInt(Cursor c,String str){
        if((c.getString(c.getColumnIndex(str)).length()==1)&&(c.getString(c.getColumnIndex(str)).contains("-"))){return -1;}
        else{return (Integer)c.getInt(c.getColumnIndex(str));}
    }
    public String nullCheckForIntSecond(int target){
        if(target==-1){return "-";}
        else{return String.format(Locale.CANADA,"%,d", target);}
    }
    public double nullCheckForDouble(Cursor c,String str){
        if((c.getString(c.getColumnIndex(str)).length()==1)&&(c.getString(c.getColumnIndex(str)).contains("-"))){return -1;}
        else{return c.getDouble(c.getColumnIndex(str));}
    }
    public String nullCheckForDoubleSecond(double target){
        if(target==-1){return "-";}
        else{return String.format(Locale.CANADA,"%,.2f", target);}
    }
    private class JsoupAsyncTask extends AsyncTask<Void,Void,Void>{
        ProgressBar progressBar;
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            //Jsoup작업 전 종목검색,스피너,뷰,버튼 기능중지
            editText.setEnabled(false);
            //sp.setEnabled(false);
            mRecyclerView.setVisibility(mRecyclerView.GONE);
            htmlTitleButton.setEnabled(false);
            Log.i("JsoupAsyncTask","JsoupAsyncTask안에 onPreExecute");
        }
        @Override
        protected Void doInBackground(Void... params) {
            /*progressBar =(ProgressBar)findViewById(R.id.progressBar);
            Log.i("JsoupAsyncTask","JsoupAsyncTask안에 doInBackground");
            while(pageCheck) {
                try {
                    Document doc = Jsoup.connect(htmlPageUrl).get();        //페이지를 가져옴
                    Elements titles = doc.select("td");     //td 태그가 있는 것들을 긁어옴
                    Log.i("mainActivity","FOR문 들어가기 전!!");
                    for (Element e : titles) {
                        if (e.html().contains("nbsp") | e.html().contains("page")) continue; //nbsp 나 page가 있는 태그는 건너뜀
                        switch(forNum) {
                            case 0: bindArgs[forNum] = e.text().trim();                       break;//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + e.text().trim());
                            case 1: if((e.text().trim().length()==1)&(e.text().trim().contains("-"))){bindArgs[forNum]=e.text().trim();  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + e.text().trim());
                            else{bindArgs[forNum]=intConvert(e.text().trim());  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + sqlInt);
                            case 2: bindArgs[forNum] = e.text().trim();                         break;//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + e.text().trim());
                            case 3: if((e.text().trim().length()==1)&(e.text().trim().contains("-"))){bindArgs[forNum]=e.text().trim();  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + e.text().trim());
                            else{bindArgs[forNum]=intConvert(e.text().trim());  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + sqlInt);
                            case 4: if((e.text().trim().length()==1)&(e.text().trim().contains("-"))){bindArgs[forNum]=e.text().trim();  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + e.text().trim());
                            else{bindArgs[forNum]=doubleConvert(e.text().trim());  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + sqlDouble);
                            case 5: if((e.text().trim().length()==1)&(e.text().trim().contains("-"))){bindArgs[forNum]=e.text().trim();  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + e.text().trim());
                            else{bindArgs[forNum]=doubleConvert(e.text().trim());  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + sqlDouble);
                            case 6: if((e.text().trim().length()==1)&(e.text().trim().contains("-"))){bindArgs[forNum]=e.text().trim();  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + e.text().trim());
                            else{bindArgs[forNum]=doubleConvert(e.text().trim());  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + sqlDouble);
                            case 7: if((e.text().trim().length()==1)&(e.text().trim().contains("-"))){bindArgs[forNum]=e.text().trim();  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + e.text().trim());
                            else{bindArgs[forNum]=doubleConvert(e.text().trim());  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + sqlDouble);
                            case 8: if((e.text().trim().length()==1)&(e.text().trim().contains("-"))){bindArgs[forNum]=e.text().trim();  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + e.text().trim());
                            else{bindArgs[forNum]=doubleConvert(e.text().trim());  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + sqlDouble);
                            case 9: if((e.text().trim().length()==1)&(e.text().trim().contains("-"))){bindArgs[forNum]=e.text().trim();  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + e.text().trim());
                            else{bindArgs[forNum]=intConvert(e.text().trim());  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + sqlInt);
                            case 10: if((e.text().trim().length()==1)&(e.text().trim().contains("-"))){bindArgs[forNum]=e.text().trim();  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + e.text().trim());
                            else{bindArgs[forNum]=intConvert(e.text().trim());  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + sqlInt);
                            case 11: if((e.text().trim().length()==1)&(e.text().trim().contains("-"))){bindArgs[forNum]=e.text().trim(); break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + e.text().trim());
                            else{bindArgs[forNum]=intConvert(e.text().trim());  break;}//Log.i("mainActivity", "bindArgs[" + (forNum) + "] =" + sqlInt);

                        }
                        progressBar.setProgress(forNum);
                        forNum++;

                        if(forNum==12) { insertD(); forNum=0; }
                    }
                    String lastPage = doc.wholeText();      //가져온 페이지에 전체 text를 String으로 가져옴
                    if(!lastPage.contains("맨뒤")){pageCheck=false;}
                    //Log.i("while문에서 pageCheck 확인","while문에서 pageCheck 확인"+pageCheck);
                    //Log.i("while문에서 htmlPageUrl 확인","while문에서 htmlPageUrl 확인"+htmlPageUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pageNum++;
                switch (kindOf) {
                    case 0:pageUrl="?&page=";htmlPageUrl = htmlPageUrlFull + pageUrl + pageNum; break;
                    case 1:pageUrl="&page="; htmlPageUrl = htmlPageUrlKospi + pageUrl + pageNum; break;
                    case 2:pageUrl="&page="; htmlPageUrl = htmlPageUrlKosdaq + pageUrl + pageNum; break;
                }
                //Log.i("mainActivity","htmlPageUrl"+htmlPageUrl);
            }*/
            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            System.out.println("JsoupAsyncTask안에 onPostExecute");
            super.onPostExecute(result);
            select(); // DB에 저장된 정보를 커서로 가져옴
            // Adapter 생성
            mAdapter = new RecyclerViewAdapter(items);
            mRecyclerView.setAdapter(mAdapter);
            //Jsoup작업 후 스피너, 뷰, 버튼 기능 되게
            editText.setEnabled(true);
            //sp.setEnabled(true);
            mRecyclerView.setVisibility(mRecyclerView.VISIBLE);
            htmlTitleButton.setEnabled(true);
            jsoupAsyncTask=null;

        }
    }
    //sqlite 관련
    public void onDestroy(){
        super.onDestroy();
        //closeDb();
    }
    void createDb(){ // onCreate() 에서 주로 호출
        db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        String sql
                = "create table "+T_NAME+"(_SEQ integer primary key autoincrement, " +
                "SYMBOL text, NAME text, YIELD text, PRICE text, DIVIDEND text, EXDIVDATE text, PAYDATE text)";
        try{
            db.execSQL(sql);
        }catch(SQLException se){
            //Log.i("DB 테이블 생성에서 오류","오류메세지"+se);
        }
        //Log.i("DB 관련","DB 생성됨 그리고 TABLE 생성됨");
    }
    void closeDb(){ // onDestroy() 에서 주로 호출
        String sql
                = "drop table "+T_NAME;
        if(db!=null) {
            try{
                db.execSQL(sql);
            }catch(SQLException se){
            }
            db.close();
            this.deleteDatabase(DB_NAME);
        }
        //Log.i("DB 관련","TABLE 삭제됨 그리고 DB 닫힘 그리고 DB 삭제됨");
    }
    void insertD(){
        tableRow=1;
        String sql
                = "insert into "+T_NAME+"(SYMBOL, NAME, YIELD,  PRICE, DIVIDEND, EXDIVDATE, PAYDATE) "
                +"values(?, ?, ?,?,?,?,?)";
        try{
            //Object bindArgs[] = {et2.getText().toString(), et3.getText().toString()};
            db.execSQL(sql, bindArgs);
            //Log.i("DB 관련","INSERT 완료: row ="+tableRow);
        }catch(SQLException se){
        }tableRow++;
    }
    //sql에서 데이터 가져오기
    public void select() {
        //Log.i("Cursor로 가져오기","select문 들어옴");
        // 1) db의 데이터를 읽어와서, 2) 결과 저장, 3)해당 데이터를 꺼내 사용
        db = helper.getReadableDatabase(); // db객체를 얻어온다. 읽기 전용
        c = db.query("DIVIDEND", null, null, null, null, null, null);
        //Log.i("Cursor로 가져오기","Cursor c 로 db쿼리문 돌림 ");
        /*
         * 위 결과는 select * from DIVIDEND 가 된다. Cursor는 DB결과를 저장한다. public Cursor
         * query (String table, String[] columns, String selection, String[]
         * selectionArgs, String groupBy, String having, String orderBy)
         */
        items.add(new Item(Integer.toString(R.string.seq),
                Integer.toString(R.string.stockSymbol),
                Integer.toString(R.string.companyName),
                Integer.toString(R.string.dividendYield),
                Integer.toString(R.string.closingPrice),
                Integer.toString(R.string.annualizedDividend),
                Integer.toString(R.string.exdivDate),
                Integer.toString(R.string.payDate)
        ));
        while (c.moveToNext()) {
            // c의 int가져와라 ( c의 컬럼 중 id) 인 것의 형태이다.
            int priceInt=nullCheckForInt(c,"PRICE");
            int annualDividendInt=nullCheckForInt(c,"DIVIDEND");
                            /*int dividendInt=nullCheckForInt(c,"DIVIDEND");
                            int oneInt=nullCheckForInt(c,"ONE");
                            int twoInt=nullCheckForInt(c,"TWO");
                            int threeInt=nullCheckForInt(c,"THREE");*/
            double yieldDouble = nullCheckForDouble(c,"YIELD");
                            /*double dpratioDouble = nullCheckForDouble(c,"DPRATIO");
                            double roeDouble = nullCheckForDouble(c,"ROE");
                            double perDouble = nullCheckForDouble(c,"PER");
                            double pbrDouble = nullCheckForDouble(c,"PBR");*/

            String seq = c.getString(c.getColumnIndex("_SEQ"));
            String symbol = c.getString((c.getColumnIndex("SYMBOL")));
            String name = c.getString(c.getColumnIndex("NAME"));
            String yield = nullCheckForDoubleSecond(yieldDouble);
            String price = nullCheckForIntSecond(priceInt);
            String dividend = nullCheckForIntSecond(annualDividendInt);
            String exdivDate = c.getString(c.getColumnIndex("EXDIVDATE"));
            String payDate = c.getString(c.getColumnIndex("PAYDATE"));
                            /*String dividend = nullCheckForIntSecond(dividendInt);
                            String dpratio = nullCheckForDoubleSecond(dpratioDouble);
                            String roe = nullCheckForDoubleSecond(roeDouble);
                            String per = nullCheckForDoubleSecond(perDouble);
                            String pbr = nullCheckForDoubleSecond(pbrDouble);
                            String one = nullCheckForIntSecond(oneInt);
                            String two= nullCheckForIntSecond(twoInt);
                            String three= nullCheckForIntSecond(threeInt);*/
            items.add(new Item(seq, symbol, name, yield, price, dividend, exdivDate,payDate));
            //Log.i("Cursor로 가져오기", "while문에서 items객체 생성!!!! ");
        }
    }
    void deleteExistingDBFile(){
        File dbFile = new File("/data/data/apps.mjk.dividend/databases/dividend.db");
        if(dbFile.exists()){
            dbFile.delete();
            //Log.i("기존DB파일","기존DB파일 삭제함");
        }
    }
}
