$(document).ready(function(){
    $("#addStar").click(function(e) {
        let form = $('#add-star-form')[0];
        if(form.checkValidity() === false){
            e.preventDefault();
            e.stopPropagation();
            form.classList.add('was-validated');
            return;
        }
        $("#addStar").prop("disabled", true);
        $.ajax({
            type : "POST",
            url : "/star/singlestar",
            data : {
                "name" : $("#name").val(),
                "year" : $("#year").val()
            },
            dataType : "json",
        }).done(function(data) {
            console.log(data);
            if (data.data === "success") {
                alert(data.type +" id: " + data.insertId + " success!");
            } else {

                alert(data.type + " fail!");
            }
        }).always(
            function(){
                form.reset();
                $('#addStar').prop("disabled", false);
            });
    });
    $("#addMovie").click(function(e) {
        let form = $('#add-movie-form')[0];
        if(form.checkValidity() === false){
            e.preventDefault();
            e.stopPropagation();
            form.classList.add('was-validated');
            return;
        }
        $("#addMovie").prop("disabled", true);
        $.ajax({
            type : "POST",
            url : "/movie/singlemovie",
            data : {
                "movieTitle" : $("#movieTitle").val(),
                "movieYear" : $("#movieYear").val(),
                "movieDirector" : $("#movieDirector").val(),
                "starName" : $("#starName").val(),
                "genreName" : $("#genreName").val()
            },
            dataType : "json",
        }).done(function(data) {
            console.log(data);
            if (data.data === "success") {
                alert(data.type +" success! movieId: " + data.movieid + " genreId: "+ data.genreid + " starId: " + data.starid);
            } else {
                // $("#add-movie-form").val("");
                alert(data.type + " fail! movieId: " + data.movieid + " genreId: "+ data.genreid + " starId: " + data.starid);
            }
        }).always(
            function(){
                form.reset();
                $('#addMovie').prop("disabled", false);
            });
    });

    $('.table-link').click(function() {
        let me = $(this);
        $('#modalData').html('');
        $.ajax({
            url: "/_dashboard/table",
            data: {"name": me.text()},
            dataType: "json"
        }).done(function(data) {

            let element = $(document.createElement("dt"));
            element.text(data['name']).addClass("col-sm").css('font-size', '1.5em');
            $('#modalData').append(element);

            for(let col of data['columns']) {
                element = $(document.createElement("dt"));
                element.text(col['name']).addClass("col-sm-3");
                $('#modalData').append(element);
                element = $(document.createElement('dd'));
                element.text(col['typeName']).addClass("col-sm");
                $('#modalData').append(element);
            }
        })
    });
});








