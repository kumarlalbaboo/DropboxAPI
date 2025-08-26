package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DropboxIntegration {

    private static final String ACCESS_TOKEN = "sl.u.AF801EPGsRQtylR42agdg_MTemLxpldGMvDuP56-6uFpg0ue1tw5rDx1hLc-5NLey79yujIgnt_ymERN_9Vuq9RenScxNmTNzr-y4Fe-SIjR2DxUxoLB1ftS7197HS3SdVZC427Jb6q79DPvw9_IpUse09cykkq-1fkrCll9W31l_xOliMbEjbCqdqBDxlCtxquIUBft-f3vJ20eNTrw-L5N8AdMZia5Aeh9UNd4O6qwsagUZ1tNc6WxHDSs7btLezR60U-cJCJIJ2YqrsDpMHSbp__StTxyykJIuIn1X_XW7L3lFBw3ZZ1oibvV0pdqqabO6HDGcUu5XiY3uRLkGhKXVJI7DK_hbaOsUR0j1qQ9_zvUPJsTt4IHyz831cHZXT7tu0ibrV3QAR-eh89f8ALVRcWhNqAS3oUXCY0u9h-fkmPLMwgXU1xRTyohzt3VTGHwIX3p6G04uM4RYEELEDDn00VrS5PzmDk7HBR3Rkywah6Ms18FPE6HRkjPCOD-oesOtlwVYZhGWWEYtpuEabJPi6ahucDqa1nZOiXwz3TGTfLwBelUjAXz7zruT4j4oYQvoluj1tr9BhyPn7xiM2zshsMW4IYeqf-4o5cauzt7lP5oNrAQIFIyJPGYTiGtzO5NybTDTgdUPpASogoNb4enR0sIv0H6c1E93IILuVhY-1O5_V0O2_oi0GiBCqRE6EzYHInswZrKwoICXdJGGnm3LLPCzYmzST_KCOAVuf5sTyu4KAKSVsF2DiMDmWmaWYFSdUsCWL3QOENbfPgbV1mfEQ22ER33GDnlMhg0Q_0YL3rs-YgbjhDiiy1hLj_OKt95-n32UOwCpNupGS5ueOP-QbRDXo3J4QyNmOeTUYnmJIHHCAxuE6aNIowEHs1ju57dD9j6sMbku842QztBHDktyMxe9G6u31wsFs_gi13JFNmZz49ebuKTahGd4uDnXqiKvSTLe5Njb_RaY5lW4Is3E8_wjxDD6wDJGyAbv5ntX82V5onnoqJ1p7STxrwX7d_uqFQGuKo6XVf3fe41Vw0KYxkbFcSsnz1r99TakRSum3US5QPBgusXUlaSUzzp4rsVbaDr-wJdVeTbpmtD2PPUsqSkVNoUDqoRwsCgLVQqXA";
    private static final String TEAM_INFO_URL = "https://api.dropboxapi.com/2/team/get_info";

    public static void main(String[] args) throws IOException, InterruptedException {

        // Create HTTP client
        HttpClient client = HttpClient.newHttpClient();

        // Build the JSON request body, which is an empty object
        String jsonBody = "{}";

        // Build request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TEAM_INFO_URL))
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Send request
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Print raw response
        System.out.println("Raw Response: " + response.body());

        // Parse JSON response
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);

        // Print specific fields
        System.out.println("Team Name: " + json.get("name").getAsString());
        System.out.println("Team ID: " + json.get("team_id").getAsString());
        System.out.println("Licensed Users: " + json.get("num_licensed_users").getAsInt());
        System.out.println("Provisioned Users: " + json.get("num_provisioned_users").getAsInt());
    }
}
