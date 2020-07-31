let query = window.location.href.split("?");
let vars = query[1].split("&");
let params = new Map();
let ORDERING1 = ["titleAsc", "titleDesc"];
let ORDERING2 = ["ratingAsc", "ratingDesc"];
for (let i=0;i<vars.length;i++) {
    let pair = vars[i].split("=");
    params.set(pair[0],pair[1]);
}

if (!params.has("limit")) {
    params.set("limit", nowLimit);
}
if (!params.has("offset")) {
    params.set("offset", nowOffset);
}

function setPageHref(n) {
    let addr = query[0]+"?";
    for (let i of params.keys()) {
        if(i === "offset") {
            addr = addr + i + "=" + (n-1) * params.get("limit") + "&";
            continue;
        }
        addr = addr + i + "=" + params.get(i) + "&";
    }
    if (addr.substring(addr.length-1,addr.length) === "&") addr = addr.substring(0,addr.length-1);
    return addr;
}

function generatePagination() {
    let nowPage = Math.ceil((nowOffset+1) / nowLimit);
    let pageNav = $('#pagenav-list');
    // prev
    let newElem = $(document.createElement("li"));
    newElem.addClass("page-item");
    if(nowPage > 1){
        let a = $(document.createElement("a"));
        a.addClass("page-link").attr("href", setPageHref(nowPage-1)).text("Prev");
        newElem.append(a);
    }
    else {
        newElem.addClass("disabled");
        let a = $(document.createElement("a"));
        a.addClass("page-link").attr("href", "#").text("Prev");
        newElem.append(a);
    }
    pageNav.append(newElem);
    // page item
    let startPage = Math.max(1, nowPage-2);
    let endPage = Math.min(Math.max(5, nowPage+2), maxPage);
    for(let i = startPage; i <= endPage; ++ i){
        newElem = $(document.createElement("li")).addClass("page-item");
        let a = $(document.createElement("a"));
        a.addClass("page-link").attr("href", setPageHref(i)).text(i);
        if(i === nowPage){
            newElem.addClass("active");
        }
        newElem.append(a);
        pageNav.append(newElem);
    }
    // Next
    newElem = $(document.createElement("li"));
    newElem.addClass("page-item");
    let a = $(document.createElement("a"));
    a.addClass("page-link").text("Next");
    if(nowPage >= maxPage){
        a.attr("href", '#'); newElem.addClass("disabled");
    } else {
        a.attr("href", setPageHref(nowPage + 1));
    }
    newElem.append(a);
    pageNav.append(newElem);
}

function setSelect(){
    let pageSize = params.get("limit");
    if (pageSize === undefined) pageSize = 25;
    let tmp = $(document.getElementById(pageSize+"PageSize"));
    tmp.attr("selected","selected");
    // alert(pageSize)

    for(let a of ORDERING1){
        for(let b of ORDERING2) {
            let opt = $(document.createElement("option"));
            opt.attr("id", a + b);
            opt.val(a + "~" + b);
            opt.text(a + ' | '+b);
            $('#selectOrder').append(opt);
        }
    }
    for(let a of ORDERING2){
        for(let b of ORDERING1){
            let opt = $(document.createElement("option"));
            opt.attr("id", a + b);
            opt.val(a + "~" + b);
            opt.text(a + ' | '+b);
            $('#selectOrder').append(opt);
        }
    }
    let order = params.get("order");
    if (order === undefined) order = ORDERING1[0] + ORDERING2[0];

    tmp = $(document.getElementById(order.replace("~", "")));
    tmp.attr("selected","selected");
    // alert(order)
}

$(document).ready(function () {
    generatePagination();
    setSelect();

    $("#selectOrder").change(function () {
        let order = $(this).children("option:selected").val();
        let addr = query[0] + "?";

        params.set("order",order);
        for (let i of params.keys()) {
            addr = addr + i + "=" + params.get(i) + "&";
        }
        if (addr.substring(addr.length-1,addr.length) == "&") addr = addr.substring(0,addr.length-1);
        // alert(addr)
        window.location.href = addr;
    });

    $("#selectPageSize").change(function () {
        let pageSize = $(this).children("option:selected").val();
        let addr = query[0] + "?";
        let sign = 0;
        for (let i of params.keys()) {
            if(i == "limit") {
                addr = addr + i + "=" + pageSize + "&";
                sign = 1;
                continue;
            }
            addr = addr + i + "=" + params.get(i) + "&";
        }
        if (sign == 0) addr = addr + "limit" + "=" + pageSize;
        if (addr.substring(addr.length-1,addr.length) == "&") addr = addr.substring(0,addr.length-1);
        // alert(addr)
        window.location.href = addr;
    });

});