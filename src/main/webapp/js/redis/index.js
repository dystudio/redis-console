/**
 * Created by trustme on 2017/1/15.
 * index.js
 */

$(function () {
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
        };
        $("#fileForm").ajaxSubmit(options);
        $("#recover").val("");
    });
    //序列化恢复
    $("#serializeRecover").on("change", function () {
        var options = {
            url: ctx + "/serializeRecover",
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
        };
        $("#serializeFileForm").ajaxSubmit(options);
        $("#serializeRecover").val("");
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

});