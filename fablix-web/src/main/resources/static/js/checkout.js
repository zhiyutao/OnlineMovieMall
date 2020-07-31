function calculatePrice(data, id) {
    let a = 0;
    let total = 0;
    Object.values(data).forEach(value => {
        if(value.movieId === id)
            a = value.singlePrice * value.count;
        total += value.singlePrice * value.count;
    })
    return [total.toFixed(2), a.toFixed(2)];
}

function calculateTotalPrice() {
    let elements = $('.price');
    let res = 0;
    for(let i = 0; i < elements.length; ++ i){
        res += Number(elements[i].textContent);
    }
    $('#totalPrice').text(res.toFixed(2));
}
$(document).ready(function () {
    calculateTotalPrice();
    $(".decrement.btn").click(function() {
        let group = $(this).parents(".count-group");
        let id = group.attr("id").split('-')[1];
        let countInput = group.find('input');
        let countNum = Number(countInput.attr("value"));
        if(countNum <= 1)
            return;
        group.find(".btn").addClass("disabled");
        console.log(id);
        $.ajax({
            url: "/cart/",
            method: "PUT",
            data: {"id": id, "count": countNum-1},
            success: function(data, textStatus) {
                console.log(data);
                countInput.attr("value", countNum-1);
                let prices = calculatePrice(data, id);
                $('#totalPrice').text(prices[0]);
                $('#price-' + id).text(prices[1]);
            },
            error: function() {
                alert("Something wrong happened!");
            }
        }).always(()=>{group.find(".btn").removeClass("disabled");});
    });

    $(".increment.btn").click(function () {
        let group = $(this).parents(".count-group");
        let id = group.attr("id").split('-')[1];
        let countInput = group.find('input');
        let countNum = Number(countInput.attr("value"));
        group.find(".btn").addClass("disabled");
        console.log(id);
        $.ajax({
            url: "/cart/",
            method: "PUT",
            data: {"id": id, "count": countNum+1},
            success: function(data, textStatus) {
                console.log(data);
                countInput.attr("value", countNum+1);
                let prices = calculatePrice(data, id);
                $('#totalPrice').text(prices[0]);
                $('#price-' + id).text(prices[1]);
            },
            error: function() {
                alert("Something wrong happened!");
            }
        }).always(()=>{group.find(".btn").removeClass("disabled");});
    });

    $(".delete.btn").click(function() {
        let group = $(this).parents(".count-group");
        let id = group.attr("id").split('-')[1];
        group.find(".btn").addClass("disabled");
        console.log(id);
        $.ajax({
            url: "/cart/",
            method: "DELETE",
            data: {"id": id},
            success: function(data, textStatus) {
                console.log(data);
                if(Object.values(data).length === 0){
                    let root = group.parents(".root.container");
                    root.html('<h3>Your shopping cart is empty!</h3>');
                } else {
                    group.parents("tr").remove();
                    let prices = calculatePrice(data, id);
                    $('#totalPrice').text(prices[0]);
                }
            },
            error: function() {
                alert("Something wrong happened!");
                group.find(".btn").removeClass("disabled");
            }
        });
    });

    $("#submitCard").click(function (e) {
        let form = $(".needs-validation")[0];
        if(form.checkValidity() === false) {
            e.preventDefault();
            e.stopPropagation();
            form.classList.add('was-validated');
            return;
        }
        form.classList.add('was-validated');
        $("#submitCard").addClass("disabled");

        $.ajax({
            type : "POST",
            url : "/checkout",
            data : {
                "firstName" : $("#firstName").val(),
                "lastName" : $("#lastName").val(),
                "cardNumber" : $("#cardNumber").val(),
                "expirationDate" : $("#expirationDate").val()
            },
            dataType : "json",
        }).done(function (data) {
            if (data.data === "success") {
                $("#root-container").html(data.html);
                calculateTotalPrice();
                $('#checkout').remove();
            } else {
                // form.reset();
                alert("Payment fail: " + data.reason);
                $('#submitCard').removeClass("disabled");
            }
        });
    });
});