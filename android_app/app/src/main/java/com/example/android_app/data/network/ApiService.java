package com.example.android_app.data.network;

import com.example.android_app.model.Email;
import com.example.android_app.model.EmailRequest;
import com.example.android_app.model.Label;
import com.example.android_app.model.LabelCreateRequest;
import com.example.android_app.model.LabelUpdateRequest;
import com.example.android_app.model.LoginRequest;
import com.example.android_app.model.LoginResponse;
import com.example.android_app.model.MailLabelRequest;
import com.example.android_app.model.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;


public interface ApiService {

    @Multipart
    @POST("api/users")
    Call<ResponseBody> registerUser(
            @Part("fullName") RequestBody fullName,
            @Part("emailAddress") RequestBody email,
            @Part("birthDate") RequestBody birthDate,
            @Part("gender") RequestBody gender,
            @Part("password") RequestBody password,
            @Part MultipartBody.Part profileImage
    );
    @POST("api/tokens")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    // GET /api/users/:id
    @GET("api/users/{id}")
    Call<User> getUserById(@Path("id") String userId);

    @POST("api/mails")
    Call<Email> createDraft( // Assuming the server returns the created draft Email object
           @Header("Authorization") String token,
           @Body EmailRequest request
    );

    @PATCH ("api/mails/{id}")
    Call<Email> updateDraft(
            @Header("Authorization") String token,
            @Path("id") String mailId,
            @Body EmailRequest request
    );

    // Send a previously saved draft
    @POST("api/mails/{mailId}/send")
    Call<ResponseBody> sendDraft(
        @Header("Authorization") String token,
        @Path("mailId") String mailId
    );

    @POST("api/mails")
    Call<Void> sendEmail(
            @Header("Authorization") String token,
            @Body EmailRequest request
    );

    // --- Mail Service Endpoints (from mailRoutes.js) ---

    // List up to 25 most recent mails (router.get('/'))
    @GET("api/mails")
    Call<List<Email>> listMails(
            @Header("Authorization") String token
    );

    // Search mails by query string in subject or body (router.get('/search/:query'))
    @GET("api/mails/search/{query}")
    Call<List<Email>> searchMails(
            @Header("Authorization") String token,
            @Path("query") String query
    );

    // GET all drafts (router.get('/drafts'))
    @GET("api/mails/drafts")
    Call<List<Email>> getDrafts(
            @Header("Authorization") String token
    );

    // GET inbox mails (router.get('/inbox'))
    @GET("api/mails/inbox")
    Call<List<Email>> getInboxEmails(
            @Header("Authorization") String token
    );

    // GET sent mails (router.get('/sent'))
    @GET("api/mails/sent")
    Call<List<Email>> getSent(
            @Header("Authorization") String token
    );

    // GET spam mails (router.get('/spam'))
    @GET("api/mails/spam")
    Call<List<Email>> getSpamMails(
            @Header("Authorization") String token
    );

    // GET deleted mails (router.get('/deleted'))
    @GET("api/mails/deleted")
    Call<List<Email>> getDeletedMails(
            @Header("Authorization") String token
    );

    // Mark a mail as important (router.post('/:id/important'))
    @POST("api/mails/{id}/important")
    Call<ResponseBody> markMailAsImportant(
            @Header("Authorization") String token,
            @Path("id") String mailId
    );

    // Unmark a mail as important (router.delete('/:id/important'))
    @DELETE("api/mails/{id}/important")
    Call<ResponseBody> unmarkMailAsImportant(
            @Header("Authorization") String token,
            @Path("id") String mailId
    );

    // Get all important mails (router.get('/important'))
    @GET("api/mails/important")
    Call<List<Email>> getImportantMails(
            @Header("Authorization") String token
    );

    // Mark a mail as starred (router.post('/:id/star'))
    @POST("api/mails/{id}/star")
    Call<ResponseBody> markMailAsStarred(
            @Header("Authorization") String token,
            @Path("id") String mailId
    );

    // Unmark a mail as starred (router.delete('/:id/star'))
    @DELETE("api/mails/{id}/star")
    Call<ResponseBody> unmarkMailAsStarred(
            @Header("Authorization") String token,
            @Path("id") String mailId
    );

    // Get all starred mails (router.get('/starred'))
    @GET("api/mails/starred")
    Call<List<Email>> getStarredMails(
            @Header("Authorization") String token
    );

    // Retrieve a single mail by ID (router.get('/:id'))
    // This is already covered by getEmailDetails, but ensuring clarity
    @GET("api/mails/{id}")
    Call<Email> getMail(
            @Header("Authorization") String token,
            @Path("id") String mailId
    );

    // Delete a mail (router.delete('/:id'))
    @DELETE("api/mails/{id}")
    Call<ResponseBody> deleteMail(
            @Header("Authorization") String token,
            @Path("id") String mailId
    );

    // Mark a mail as spam (router.post('/:id/spam'))
    @POST("api/mails/{id}/spam")
    Call<ResponseBody> markMailAsSpam(
            @Header("Authorization") String token,
            @Path("id") String mailId
    );

    // Unmark a mail as spam (router.delete('/:id/spam'))
    @DELETE("api/mails/{id}/spam")
    Call<ResponseBody> unmarkMailAsSpam(
            @Header("Authorization") String token,
            @Path("id") String mailId
    );

    // Mark mail as read (router.post('/:id/read'))
    @POST("api/mails/{id}/read")
    Call<ResponseBody> markAsRead(
            @Header("Authorization") String token,
            @Path("id") String mailId
    );

    // Mark mail as unread (router.post('/:id/unread'))
    @POST("api/mails/{id}/unread")
    Call<ResponseBody> markAsUnread(
            @Header("Authorization") String token,
            @Path("id") String mailId
    );

    // POST to labels (router.post('/:id/labels')) - Adds a label to a mail
    @POST("api/mails/{id}/labels")
    Call<ResponseBody> addLabelToMail( // Renamed to clarify context
                                       @Header("Authorization") String token,
                                       @Path("id") String mailId,
                                       @Body MailLabelRequest request // Assuming this request body specifies the label to add
    );

    // DELETE from labels (router.delete('/:id/labels/:labelId')) - Removes a label from a mail
    @DELETE("api/mails/{id}/labels/{labelId}")
    Call<ResponseBody> removeLabelFromMail( // Renamed to clarify context
                                            @Header("Authorization") String token,
                                            @Path("id") String mailId,
                                            @Path("labelId") String labelId
    );

    // GET labels for a mail (router.get('/:id/labels'))
    @GET("api/mails/{id}/labels")
    Call<List<Label>> getMailLabels( // Renamed to clarify context
                                     @Header("Authorization") String token,
                                     @Path("id") String mailId
    );

    // --- Label Service Endpoints (Existing in original ApiService) ---

    @GET("api/labels")
    Call<List<Label>> getLabels(@Header("Authorization") String token);

    @POST("api/labels")
    Call<Label> createLabel(@Header("Authorization") String token, @Body LabelCreateRequest request);

    @PATCH("api/labels/{id}")
    Call<Label> updateLabel(
            @Header("Authorization") String token,
            @Path("id") String id,
            @Body LabelUpdateRequest request
    );

    @DELETE("api/labels/{id}")
    Call<Void> deleteLabel(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    @GET("api/labels/{id}/mails")
    Call<List<Email>> getMailsByLabel(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    @POST("api/labels/{labelId}/mails")
    Call<ResponseBody> addMailToLabel( // Assuming the response might be an empty body or a simple success message
           @Header("Authorization") String token,
           @Path("labelId") String labelId,
           @Body MailLabelRequest request
    );

    // Use @HTTP for DELETE requests that require a request body
    @HTTP(method = "DELETE", path = "api/labels/{labelId}/mails", hasBody = true)
    Call<Void> removeMailFromLabel(
            @Header("Authorization") String token,
            @Path("labelId") String labelId,
            @Body MailLabelRequest request
    );

    @Multipart
    @PATCH("api/users/{id}")
    Call<LoginResponse> updateUser(
            @Path("id") String userId,
            @Part("fullName") RequestBody fullName,
            @Part MultipartBody.Part profileImage
    );

    // Duplicates from before, kept for context but new ones are more specific

    @GET("api/mails/{id}")
    Call<Email> getEmailDetails(
            @Header("Authorization") String token,
            @Path("id") String emailId
    );

}