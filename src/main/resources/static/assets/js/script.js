// Shows and hides password
function passHide() {
    var x = document.getElementById("newPassword");
    var y = document.getElementById("eye");
    if (x.type === "password") {
        x.type = "text";
        y.style.color = "#18bc18";

    } else {
        x.type = "password";
        y.style.color = "#495057";
    }
}

function passHide2() {
    var x = document.getElementById("password");
    var y = document.getElementById("eye2");
    if (x.type === "password") {
        x.type = "text";
        y.style.color = "#18bc18";

    } else {
        x.type = "password";
        y.style.color = "#495057";
    }
}

$(document).ready(function(){
    $('#addAccount').on('shown.bs.modal', function () {
        $('#addAccount').focus();
    })

    $('#add-account-dashboard').click(function(){
        $('#add-account-nav').click();
    });

    $('#add-account-submit').click(function(){
        completeLogin();
    });

    $('#username').on('keypress', function (e) {
         if(e.which === 13){
            completeLogin();
         }
    });

    $('#password').on('keypress', function (e) {
         if(e.which === 13){
            completeLogin();
         }
    });


    function completeLogin(){
      if($("#username").val().length == 0){
            $("#add-account-error").html('<div id="add-account-alert" class="alert fade show mb-0 alert-danger my-3" role="alert"><span>Username is required</span></div>');
            return;
        }

        if($("#password").val().length == 0){
            $("#add-account-error").html('<div id="add-account-alert" class="alert fade show mb-0 alert-danger my-3" role="alert"><span>Password is required</span></div>');
            return;
        }

        $('#add-account-submit').html('Loading <img class="img-fluid ml-2" src="https://insta-mation.com/assets/images/loader.gif"/>')
        $('#add-account-alert').remove();
        firstTimeLogin();
    }

    function firstTimeLogin(){
        var formData = {
            username : $("#username").val(),
            password : $("#password").val()
        }

        var userId = $("#userid").val();

        $.ajax({
            type: "POST",
            data: JSON.stringify(formData),
            url: "/account/add/" + userId,
            contentType: "application/json",
            dataType: "json",
            success: function(result){
                if(result.status == "wrong-credentials"){
                    $("#add-account-error").html('<div id="add-account-alert" class="alert fade show mb-0 alert-danger my-3" role="alert"><span>Invalid username or password</span></div>');
                    $('#add-account-submit').text('Add Account');
                } else if(result.status == "unusual-attempt") {
                    $("#add-account-error").html('<div id="add-account-alert" class="alert fade show mb-0 alert-danger my-3" role="alert"><span>Almost there! Open the Instagram APP on your phone and accept the <b class="font-weight-500">"It was me"</b> request. Then try to add the account again.</span></div>');
                    $('#add-account-submit').text('Add Account');
                } else {
                    $("#add-account-error").html('<div id="add-account-alert" class="alert fade show mb-0 alert-success my-3 text-center" role="alert"><span>Success!</span></div>');
                    window.location.replace("/dashboard");
                }
//                console.log(result);
            },
            error : function(e) {
                $("#add-account-error").html('<div id="add-account-alert" class="alert fade show mb-0 alert-danger my-3" role="alert"><span>There was an error, please try again. If the problem persists please Contact us.</span></div>');
                $('#add-account-submit').text('Add Account');

//                console.log("ERROR: ", e);
            }
        });

    	// Reset FormData after Posting
        //resetData();

        function resetData(){
            $("#username").val("");
            $("#password").val("");
        }
    }



///////////////////////////SETTINGS PAGES////////////////////////////////////////

    ///////////////////////////ACTIONS SETTINGS////////////////////////////////////////

    // load true states
    if($("#likes1").val() == 'true'){
        $("#likes2").attr('checked', 'true');
    }
    if($("#comment1").val() == 'true'){
        $("#comment2").attr('checked', 'true');
    }
    if($("#follow1").val() == 'true'){
        $("#follow2").attr('checked', 'true');
    }
    if($("#unfollow1").val() == 'true'){
        $("#unfollow2").attr('checked', 'true');
    }

    // if action settings change, change the hidden values
    sendSettingsValuesAndRequest("likes");
    sendSettingsValuesAndRequest("comment");
    sendSettingsValuesAndRequest("follow");
    sendSettingsValuesAndRequest("unfollow");

    function sendSettingsValuesAndRequest(id){
        $("#" + id + "2").change(function(){
            if($("#" + id + "1").val() == "true"){
                $("#" + id + "1").val("false");
            } else {
                $("#" + id + "1").val("true");
            }
            sendSettingsRequest();
        });
    }

    ///////////////////////////MAIN SETTINGS////////////////////////////////////////

    if($("#action-speed-val").val() == "slow"){
        $("#action-speed").val("Slow");
    } else if ($("#action-speed-val").val() == "normal"){
        $("#action-speed").val("Normal");
    } else {
        $("#action-speed").val("Fast");
    }

    if($("#media-type-val").val() == "All"){
        $("#media-type").val("All");
    } else if ($("#media-type-val").val() == "Images"){
        $("#media-type").val("Images");
    } else {
        $("#media-type").val("Video");
    }

    $("#settings").change(function(){
        sendSettingsRequest();
    });

    ///////////////////////////SETTINGS AJAX REQUEST////////////////////////////////////////

    function sendSettingsRequest(){
         var settingsData = {
            id : $("#id").val(),
            likes : $("#likes1").val(),
            comment : $("#comment1").val(),
            follow : $("#follow1").val(),
            unfollow : $("#unfollow1").val(),
            actionSpeed : $("#action-speed").val(),
            mediaType : $("#media-type").val(),
            minLikesFilter : $("#min-likes-filter").val(),
            maxLikesFilter : $("#max-likes-filter").val()
         }

         $.ajax({
             type: "POST",
             data: JSON.stringify(settingsData),
             url: "/account/save-settings",
             contentType: "application/json",
             dataType: "json",
             success: function(result){
                 if(result.status == "success"){
                      $("#settings-alert").html('<div class="alert fade show mb-0 alert-primary mb-3"  role="alert"><span>Your settings have been updated</span><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span>  </button></div>');
                 }
             },
             error : function(e) {
                 console.log("ERROR: ", e);
             }
         });
    }



    ///////////////////////////PROFILE SEED SETTINGS////////////////////////////////////////

    addNewSeed("username");
    addNewSeed("tag");

    function addNewSeed(type){

        $("#seed-" + type + "-add").click(function(){
            if($("#seed-" + type + "-name").val().length != 0){
                sendProfileSeedRequest();
                $("#seed-" + type + "-name").val("");
            }
        });

        $("#seed-" + type + "-name").on('keypress', function (e) {
             if(e.which === 13){
                if($("#seed-" + type + "-name").val().length != 0){
                  sendProfileSeedRequest();
                  $("#seed-" + type + "-name").val("");
                }
             }
        });

         function sendProfileSeedRequest(){
            var name = $("#seed-" + type + "-name").val();
            var type2 = $("#seed-" + type + "-type").val();

             var settingsData = {
                name : name,
                type : type2
             }

             var account = $("#seed-"+ type + "-account").val();

             var atOrHashtag = (type == 'username') ? '@' : '#';
             var tagsOrUsernames = (type == 'username') ? 'usernames' : 'tags';

             $.ajax({
                 type: "POST",
                 data: JSON.stringify(settingsData),
                 url: "/account/seed/add?type=" + type + "&account_id=" + account,
                 contentType: "application/json",
                 dataType: "json",
                 success: function(result){
                     if(result.status == "success"){
                          $("#seed-settings-alert").html('<div class="alert fade show mb-0 alert-primary mb-3"  role="alert"><span>Your settings have been updated</span><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span>  </button></div>');
                          $("#" + tagsOrUsernames).append('<div class="col-6 col-lg-4 px-0 text-center"><span>' + atOrHashtag + name + '</span><a href="#" class="ml-1 text-danger font-weight-500" id="">x</a></div>');
                     }
                 },
                 error : function(e) {
                     console.log("ERROR: ", e);
                 }
             });
        }
    }
       ///////////////////////////DELETE TAG////////////////////////////////////////

        $("#tags a").click(function(){
            deleteSeedRequest(this.id.replace("seed-",""), "tag");
            this.offsetParent.remove();

        });

        $("#usernames a").click(function(){
            deleteSeedRequest(this.id.replace("seed-",""), "username");
            this.offsetParent.remove();
        });

       function deleteSeedRequest(tagId){

            var account = $("#seed-tag-account").val();

            $.ajax({
                type: "POST",
                url: "/account/seed/delete?account_id=" + account + "&seed_id=" + tagId,
                contentType: "application/json",
                dataType: "json",
                success: function(result){
                    if(result.status == "success"){
                         $("#seed-settings-alert").html('<div class="alert fade show mb-0 alert-primary mb-3"  role="alert"><span>Your settings have been updated</span><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span>  </button></div>');
                    }
                },
                error : function(e) {
                    console.log("ERROR: ", e);
                }
            });
        }

    ///////////////////////////COMMENT SETTINGS////////////////////////////////////////

     $("#comment-add").click(function(){
            if($("#comment").val().length != 0){
                sendCommentRequest();
                $("#comment").val("");
            }
        });

        $("#comment").on('keypress', function (e) {
             if(e.which === 13){
                 if($("#comment").val().length >= 400){
                    return;
                 }

                if($("#comment").val().length != 0){
                  sendCommentRequest();
                  $("#comment").val("");
                }
             }
        });

         function sendCommentRequest(){

             var description = $("#comment").val();
             var settingsData = {
                description : description
             }

             var account = $("#comment-account").val();


             $.ajax({
                 type: "POST",
                 data: JSON.stringify(settingsData),
                 url: "/account/comment/add?account_id=" + account,
                 contentType: "application/json",
                 dataType: "json",
                 success: function(result){
                     if(result.status == "success"){
                          $("#seed-settings-alert").html('<div class="alert fade show mb-0 alert-primary mb-3"  role="alert"><span>Your settings have been updated</span><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span>  </button></div>');
                          $("#comments").append('<p class="mb-1 text-secondary text-center"><span>' + description + '</span><a href="#" class="ml-1 text-danger font-weight-500" id="">x</a></p>');
                     }
                 },
                 error : function(e) {
                     console.log("ERROR: ", e);
                 }
             });
       }

       ///////////////////////////DELETE COMMENT////////////////////////////////////////

        $("#comments a").click(function(){
            deleteCommentRequest(this.id.replace("comment-", ""));
            this.parentElement.remove();
        });

       function deleteCommentRequest(commentId){

            var account = $("#comment-account").val();

            $.ajax({
                type: "POST",
                url: "/account/comment/delete?account_id=" + account + "&comment_id=" + commentId,
                contentType: "application/json",
                dataType: "json",
                success: function(result){
                    if(result.status == "success"){
                         $("#seed-settings-alert").html('<div class="alert fade show mb-0 alert-primary mb-3"  role="alert"><span>Your settings have been updated</span><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span>  </button></div>');
                    }
                },
                error : function(e) {
                    console.log("ERROR: ", e);
                }
            });
        }



});


