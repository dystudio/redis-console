/**
 * Created by trustme on 2017/1/15.
 * index.js
 */
$("#string").on("click",function () {
    window.location.href=ctx+"/string";
});
$("#backup").on("click", function () {
    open(ctx+"/backup");
});
$("#recover").on("change", function () {
    var options = {
        url: ctx+"/recover",
        type: "post",
        dataType: "json",
        success: function (data) {
            alert(data)
        }
    }
    $("#fileForm").ajaxSubmit(options);
    $("#recover").val("");
});
$("#flushAll").on("click",function(){
    $.ajax({
        url:ctx+"/flushAll",
        type:"post",
        dataType:"json",
        success:function (data) {
            alert(data)
        }
    })
});
function deleteCustomer(id) {
    if (confirm('确实要删除该客户吗?')) {
        $.post(ctx+"customer/delete.action", {"id": id}, function (data) {
            alert("客户删除更新成功！");
            window.location.reload();
        });
    }
}