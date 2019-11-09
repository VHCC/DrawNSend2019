package ichen.chu.drawnsend.api;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

import ichen.chu.drawnsend.Bus;
import ichen.chu.drawnsend.BusEvent;
import ichen.chu.drawnsend.model.DnsResult;
import ichen.chu.drawnsend.util.MLog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static ichen.chu.drawnsend.App.SERVER_SITE;
import static ichen.chu.drawnsend.Bus.EVENT_MAP;
import static ichen.chu.drawnsend.Bus.EVENT_PLAY_BOARD_UPLOAD_FILE_DONE;
import static ichen.chu.drawnsend.api.APICode.API_CREATE_GAME_CHAIN;
import static ichen.chu.drawnsend.api.APICode.API_FETCH_ROOM_INFO;
import static ichen.chu.drawnsend.api.APICode.API_GET_FOLDER_ID;
import static ichen.chu.drawnsend.api.APICode.API_GET_GAME_SUBJECT;
import static ichen.chu.drawnsend.api.APICode.API_GET_PLAYER_ORDERS;
import static ichen.chu.drawnsend.api.APICode.API_UPDATE_ROOM_STATUS;

/**
 * Created by IChen.Chu on 2019/11/5
 */
public class DnsServerAgent {

    private static final MLog mLog = new MLog(true);
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    private static DnsServerAgent mDnsServerAgent = null;

    private Context mContext;

    // constructor
    private DnsServerAgent(Context context) {
        this.mContext = context;
    }

    public static DnsServerAgent getInstance(Context context) {
        if (null == mDnsServerAgent) {
            synchronized (DnsServerAgent.class) {
                mDnsServerAgent = new DnsServerAgent(context);
            }
        }
        return mDnsServerAgent;
    }

    // API
    public void createPlayRoom(final Handler SADHandler,
                                final int playTime,
                                final int difficulty,
                                final boolean isAdult) {
        try {
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(mContext);

            JSONObject userObj = new JSONObject();
            userObj.put("email", acct.getEmail());
            userObj.put("displayName", acct.getDisplayName());
            userObj.put("photoUrl", acct.getPhotoUrl());

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("roomOwner", userObj);
            jsonObj.put("playTime", playTime);
            jsonObj.put("difficulty", difficulty);
            jsonObj.put("isAdult", isAdult);

            RequestBody requestBody = RequestBody.create(JSON, jsonObj.toString());

            Request request = new Request.Builder()
                    .url(SERVER_SITE + "/api/post_dns_create_game_room")
//                        .url("https://dns.ichenprocin.dsmynas.com/api/get_dns_check_server_status")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJ = new JSONObject(response.body().string());
//                                mLog.d(TAG, "response= " + responseJ);
//                            mLog.d(TAG, "payload= " + responseJ.get("payload"));
                            Message msg = new Message();
                            msg.obj = responseJ.get("payload");
                            SADHandler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
        }
    }

    public void updatePlayRoomStatus(final Handler SADHandler,
                                     final String roomNumberCode,
                                     final int roomStatus) {
        try {

            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(mContext);


            JSONObject jsonObj = new JSONObject();
            jsonObj.put("joinNumber", roomNumberCode);
            jsonObj.put("roomStatus", roomStatus);

            RequestBody requestBody = RequestBody.create(JSON, jsonObj.toString());

            Request request = new Request.Builder()
                    .url(SERVER_SITE + "/api/post_dns_update_room_status")
//                        .url("https://dns.ichenprocin.dsmynas.com/api/get_dns_check_server_status")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        JSONObject responseJ = null;
                        try {
                            responseJ = new JSONObject(response.body().string());
//                                mLog.d(TAG, "response= " + responseJ);
//                            mLog.d(TAG, "payload= " + responseJ.get("payload"));
                            Message msg = new Message();
                            msg.obj = responseJ.getInt("payload");
                            msg.arg1 = API_UPDATE_ROOM_STATUS;
                            SADHandler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        } catch (Exception e) {
            mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
        }
    }

    public void fetchPlayRoomInfo(final Handler SADHandler,
                                   final String roomNumberCode) {
        try {

            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(mContext);

            JSONObject userObj = new JSONObject();
            userObj.put("email", acct.getEmail());
            userObj.put("displayName", acct.getDisplayName());
            userObj.put("photoUrl", acct.getPhotoUrl());

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("player", userObj);
            jsonObj.put("joinNumber", roomNumberCode);

            RequestBody requestBody = RequestBody.create(JSON, jsonObj.toString());

            Request request = new Request.Builder()
                    .url(SERVER_SITE + "/api/post_dns_fetch_room_info")
//                        .url("https://dns.ichenprocin.dsmynas.com/api/get_dns_check_server_status")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {

                        JSONObject responseJ = null;
                        try {
                            responseJ = new JSONObject(response.body().string());
//                                mLog.d(TAG, "response= " + responseJ);
//                                mLog.d(TAG, "payload= " + responseJ.get("payload"));
                            Message msg = new Message();
                            msg.obj = responseJ.get("payload");
                            msg.arg1 = API_FETCH_ROOM_INFO;
                            SADHandler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        } catch (Exception e) {
            mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
        }
    }

    public void joinPlayRoom(final Handler SADHandler,
                              final String roomNumberCode) {
        try {

            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(mContext);

            JSONObject userObj = new JSONObject();
            userObj.put("email", acct.getEmail());
            userObj.put("displayName", acct.getDisplayName());
            userObj.put("photoUrl", acct.getPhotoUrl());

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("player", userObj);
            jsonObj.put("joinNumber", roomNumberCode);

            RequestBody requestBody = RequestBody.create(JSON, jsonObj.toString());

            Request request = new Request.Builder()
                    .url(SERVER_SITE + "/api/post_dns_join_game_room")
//                        .url("https://dns.ichenprocin.dsmynas.com/api/get_dns_check_server_status")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJ = new JSONObject(response.body().string());
//                                mLog.d(TAG, "response= " + responseJ);
                            mLog.d(TAG, "payload= " + responseJ.get("payload"));
                            JSONObject responsePayload = new JSONObject(String.valueOf(responseJ.get("payload")));
                            mLog.d(TAG, "nModified= " + responsePayload.get("nModified"));

                            int modifiedStatus = (int) responsePayload.get("nModified");
                            Message msg;
                            switch(modifiedStatus) {
                                case 0:
                                    msg = new Message();
                                    msg.what = 0;
                                    SADHandler.sendMessage(msg);
                                    break;
                                case 1:
                                    msg = new Message();
                                    msg.what = 1;
                                    SADHandler.sendMessage(msg);
                                    break;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });

        } catch (Exception e) {
            mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
        }
    }

    public void quitPlayRoom(final Handler SADHandler,
                              final String roomNumberCode) {
        try {

            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(mContext);

            JSONObject userObj = new JSONObject();
            userObj.put("email", acct.getEmail());
            userObj.put("displayName", acct.getDisplayName());
            userObj.put("photoUrl", acct.getPhotoUrl());

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("player", userObj);
            jsonObj.put("joinNumber", roomNumberCode);

            RequestBody requestBody = RequestBody.create(JSON, jsonObj.toString());

            Request request = new Request.Builder()
                    .url(SERVER_SITE + "/api/post_dns_quit_game_room")
//                        .url("https://dns.ichenprocin.dsmynas.com/api/get_dns_check_server_status")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
//                            mLog.d(TAG, "response= " + response.body().string());
                        SADHandler.sendMessage(new Message());
                    }
                }
            });
        } catch (Exception e) {
            mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
        }
    }

    public void getGameChainFolderID(final Handler SADHandler, String gameChainFolderName) {
        try {
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(mContext);

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("folderName", acct.getEmail() + "_" + gameChainFolderName);

            RequestBody requestBody = RequestBody.create(JSON, jsonObj.toString());

            Request request = new Request.Builder()
                    .url(SERVER_SITE + "/api/post_dns_google_drive_get_folder_id")
//                        .url("https://dns.ichenprocin.dsmynas.com/api/get_dns_check_server_status")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJ = new JSONObject(response.body().string());
                            mLog.d(TAG, "getGameChainFolderID, response= " + responseJ);
                            Message msg = new Message();
                            msg.arg1 = API_GET_FOLDER_ID;
                            msg.obj = responseJ.get("folderID");
                            SADHandler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
        }
    }

    public void getRandomPlayers(final Handler SADHandler, String joinNumber) {
        try {
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(mContext);

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("joinNumber", joinNumber);

            RequestBody requestBody = RequestBody.create(JSON, jsonObj.toString());

            Request request = new Request.Builder()
                    .url(SERVER_SITE + "/api/post_dns_make_random_orders_participants")
//                        .url("https://dns.ichenprocin.dsmynas.com/api/get_dns_check_server_status")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJ = new JSONObject(response.body().string());
                            mLog.d(TAG, "response= " + responseJ);
                            Message msg = new Message();
                            msg.arg1 = API_GET_PLAYER_ORDERS;
                            msg.obj = responseJ.get("payload");
                            SADHandler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
        }
    }

    public void getSubject(final Handler SADHandler, int difficulty, boolean isAdult) {
        try {
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(mContext);

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("difficulty", difficulty);
            jsonObj.put("isAdult", isAdult);

            RequestBody requestBody = RequestBody.create(JSON, jsonObj.toString());

            Request request = new Request.Builder()
                    .url(SERVER_SITE + "/api/post_dns_game_subject_get")
//                        .url("https://dns.ichenprocin.dsmynas.com/api/get_dns_check_server_status")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJ = new JSONObject(response.body().string());
                            mLog.d(TAG, "getSubject, response= " + responseJ);
                            Message msg = new Message();
                            msg.arg1 = API_GET_GAME_SUBJECT;
                            msg.obj = responseJ.get("payload");
                            SADHandler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
        }
    }

    public void createGameChain(final Handler SADHandler,
                                String joinNumber,
                                String folderID,
                                String subject,
                                JSONArray playerOrders) {
        try {
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(mContext);

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("chainID", acct.getEmail()+joinNumber);
            jsonObj.put("joinNumber", joinNumber);
            jsonObj.put("folderID", folderID);
            jsonObj.put("subject", subject);
            jsonObj.put("creator", acct.getEmail());
            jsonObj.put("playerChained", playerOrders);

            RequestBody requestBody = RequestBody.create(JSON, jsonObj.toString());

            Request request = new Request.Builder()
                    .url(SERVER_SITE + "/api/post_dns_game_chain_create_game_chain")
//                        .url("https://dns.ichenprocin.dsmynas.com/api/get_dns_check_server_status")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJ = new JSONObject(response.body().string());
                            mLog.d(TAG, "createGameChain, response= " + responseJ);
                            Message msg = new Message();
                            msg.arg1 = API_CREATE_GAME_CHAIN;
                            msg.obj = responseJ.get("payload");
                            SADHandler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
        }
    }

    public void fetchGameChainInfo(final Handler SADHandler,
                                String joinNumber) {
        try {
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(mContext);

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("chainID", acct.getEmail() + joinNumber);

            RequestBody requestBody = RequestBody.create(JSON, jsonObj.toString());

            Request request = new Request.Builder()
                    .url(SERVER_SITE + "/api/post_dns_game_chain_get_game_chain_info")
//                        .url("https://dns.ichenprocin.dsmynas.com/api/get_dns_check_server_status")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJ = new JSONObject(response.body().string());
                            mLog.d(TAG, "fetchGameChainInfo, response= " + responseJ);
                            Message msg = new Message();
                            msg.arg1 = API_CREATE_GAME_CHAIN;
                            msg.obj = responseJ.get("payload");
                            SADHandler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
        }
    }


    // *********** Play Board ***********
    public void uploadFile(File file, String folderID) {
        try {
            try {
                final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
                mLog.d(TAG, "file.getName()= " + file.getName());
                RequestBody req = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("name", file.getName())
                        .addFormDataPart("folderID", folderID)
                        .addFormDataPart("file", file.getName(), RequestBody.create(MEDIA_TYPE_JPEG, file)).build();

                Request request = new Request.Builder()
                        .url(SERVER_SITE + "/api/post_dns_google_drive_upload_file")
                        .post(req)
                        .build();

                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(request).execute();

                DnsResult.getInstance().setResultID(response.body().string());

                mLog.d(TAG, "uploadImage, fileID= " + DnsResult.getInstance().getResultID());

                Bus.getInstance().post(new BusEvent(EVENT_MAP.get(EVENT_PLAY_BOARD_UPLOAD_FILE_DONE), EVENT_PLAY_BOARD_UPLOAD_FILE_DONE));
            } catch (UnknownHostException | UnsupportedEncodingException e) {
                mLog.e(TAG, "Error: " + e.getLocalizedMessage());
            } catch (Exception e) {
                mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateGameChainResult(String chainID, String fileID, int resultIndex) {
        try {
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(mContext);

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("chainID", chainID);
            jsonObj.put("fileID", fileID);
            jsonObj.put("resultIndex", resultIndex);

            RequestBody requestBody = RequestBody.create(JSON, jsonObj.toString());

            Request request = new Request.Builder()
                    .url(SERVER_SITE + "/api/post_dns_game_chain_get_game_chain_info")
//                        .url("https://dns.ichenprocin.dsmynas.com/api/get_dns_check_server_status")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject responseJ = new JSONObject(response.body().string());
                            mLog.d(TAG, "response= " + responseJ);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            mLog.e(TAG, "Other Error: " + e.getLocalizedMessage());
        }
    }
}
