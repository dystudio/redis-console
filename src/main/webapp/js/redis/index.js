/**
 * Created by trustme on 2017/1/15.
 * index.js
 */
$("#string").on("click", function () {
    $("#side-menu li a").removeClass("active");
    $(this).addClass("active");
    $.ajax({
        url: ctx + "/string",
        type: "post",
        dataType: "json",
        success: function (data) {
            $(".col-lg-12").empty();
            ajaxLoadData(data);
        }
    });
    // window.location.href = ctx + "/string";
});
//备份
$("#backup").on("click", function () {
    open(ctx + "/backup");
});
//恢复
$("#recover").on("change", function () {
    var options = {
        url: ctx + "/recover",
        type: "post",
        dataType: "json",
        success: function (data) {
            if (data == '1') {
                $("#promptTitle").html("成功提示");
                $("#promptContent").html("<p>恢复成功</p>");
                $("#promptBtn").removeClass("btn-danger").addClass("btn-success");
            } else {
                $("#promptTitle").html("失败提示");
                $("#promptContent").html("<p>恢复失败</p>");
                $("#promptBtn").removeClass("btn-success").addClass("btn-danger");
            }
            $("#prompt").modal("show");
        }
    }
    $("#fileForm").ajaxSubmit(options);
    $("#recover").val("");
});
//删除全部数据
$("#flushAll").on("click", function () {
    $('#myModal').modal('hide');
    $.ajax({
        url: ctx + "/flushAll",
        type: "post",
        dataType: "json",
        success: function (data) {
            if (data == '1') {
                $("#promptTitle").html("成功提示");
                $("#promptContent").html("<p>删除成功</p>");
                $("#promptBtn").removeClass("btn-danger").addClass("btn-success");
            } else {
                $("#promptTitle").html("失败提示");
                $("#promptContent").html("<p>删除失败</p>");
                $("#promptBtn").removeClass("btn-success").addClass("btn-danger");
            }
            $("#prompt").modal("show");
        }
    })
});
//提示框关闭从新加载页面
$("#prompt").on("hide.bs.modal", function () {
    location.reload();
});
function deleteCustomer(id) {
    if (confirm('确实要删除该客户吗?')) {
        $.post(ctx + "customer/delete.action", {"id": id}, function (data) {
            alert("客户删除更新成功！");
            window.location.reload();
        });
    }
}
function ajaxLoadData(data) {
    var str = '<div class="panel panel-default">' +
        '<div class="panel-heading">string</div>' +
        '<table class="table table-bordered table-striped" style="border-bottom: 1px solid #ddd">' +
        '<thead>' +
        '<tr style=" border-top: 1px solid #ddd;">' +
        '<th>key</th>' +
        '<th>value</th>' +
        '<th style="width: 95px">操作</th>' +
        '</tr>' +
        '</thead>' +
        '<tbody>';
    for (var name in data) {
        str += ' <tr>' +
            '<td>' + name + '</td>' +
            '<td>' + data[name] + '</td>' +
            '<td>' +
            '<a href="#" class="btn btn-primary btn-xs" data-toggle="modal" data-target="#customerEditDialog" >修改</a>' +
            '<a href="#" class="btn btn-danger btn-xs" >删除</a>' +
            '</td> </tr>';
    }
    str += ' </tbody> </table> </div>';
    $(".col-lg-12").append(str);

}