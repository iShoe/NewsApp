package com.example.news;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.news.api.ApiClient;
import com.example.news.api.ApiInterface;
import com.example.news.models.Article;
import com.example.news.models.News;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    public static final String API_KEY = "dc896d4db1d0476e800a8a47b249cf04";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Article> articles = new ArrayList<>();
    private Adapter adapter;
    private String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Paper.init(this);

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        LoadJson();
    }

    public void LoadJson(){
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        String country = Utils.getCountry();
        Call<News> call;
        call = apiInterface.getNews(country, API_KEY);

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if(response.isSuccessful() && response.body().getArticles() != null){
                    if(!articles.isEmpty()){
                        articles.clear();
                    }

                    articles = response.body().getArticles();
                    

                    adapter = new Adapter(articles, MainActivity.this);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();


                    // save to cache
                    Paper.book().write("cache", new Gson().toJson(response.body()));


                } else {
                    Toast.makeText(MainActivity.this, "No Result", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                Toast.makeText(MainActivity.this, "No Internet ", Toast.LENGTH_SHORT).show();

                // read from cache if no internet
                String cache = Paper.book().read("cache");
                if(cache != null && !cache.isEmpty()) {
                    News news = new Gson().fromJson(cache, News.class);
                    articles = news.getArticles();

                    adapter = new Adapter(articles, MainActivity.this);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    
                }
            }
        });

    }
}
