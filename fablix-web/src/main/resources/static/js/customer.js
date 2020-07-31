$(document).ready(function() {
    $('.cart.btn').click(function(){
        if($(this).hasClass('disabled'))
            return;
        let id = $(this).attr("id");
        let movieId = id.split('-')[1];
        let text = $(this).text();
        $(this).addClass("disabled");
        $(this).text("Proceeding...");
        let me = $(this);
        $.ajax({
            url: "/cart/",
            method: "POST",
            data: {"id": movieId, "title": me.data("title")},
            success: function(data, textStatus) {
                let a = me.next('span');
                a.hide().text('Success!');
                a.fadeIn();
                a.fadeOut({complete: function() {
                        me.text(text); me.removeClass("disabled");
                    }});
            },
            error: function() {
                let a = me.next('span');
                a.hide().text('Fail!');
                a.fadeIn();
                a.fadeOut({complete: function() {
                        me.text(text); me.removeClass("disabled");
                    }});
            }
        });

    });
})