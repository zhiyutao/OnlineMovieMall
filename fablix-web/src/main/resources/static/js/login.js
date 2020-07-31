'use strict';
var grecaptchaResp = null;
var verifyCallback = function (response) {
    grecaptchaResp = response;
};
var onloadCallback = function() {
    grecaptcha.render("grecaptcha", {
        "sitekey": "6LcUFfIUAAAAAAtamjiBjwL8ztgoG2kvKdYR-Qmn",
        "callback": verifyCallback
    });
};
$(document).ready(function(){
    $(".login.btn").click(function(e) {
        let me = $(this);
        let form = $(".needs-validation")[0];
        if(form.checkValidity() === false) {
            e.preventDefault();
            e.stopPropagation();
            form.classList.add('was-validated');
            return;
        }
        form.classList.add('was-validated');
        $(".login.btn").prop("disabled", true);
        let postUrl, redirectUrl;
        if(me.attr("id") === "customerLogIn") {
            postUrl = "/login";
            redirectUrl = "/";
        }
        else if(me.attr("id") === "employeeLogIn") {
            postUrl = "/_dashboard/login";
            redirectUrl = "/_dashboard";
        }

        $.ajax({
            type : "POST",
            url : postUrl,
            data : {
                "email" : $("#email").val(),
                "password" : $("#password").val(),
                "g-recaptcha-response": grecaptchaResp
            },
            dataType : "json",
        }).done(function(data) {
            console.log(data);
            if (data.data === "success") {
                window.location.href = redirectUrl;
            } else {
                form.reset();
                alert("Login fail: " + data.reason);
                if(grecaptchaResp != null)
                    grecaptcha.reset();
            }
        }).always(
            function(){
                $('.login.btn').prop("disabled", false);
            });
    });

});