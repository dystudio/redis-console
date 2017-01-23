/**
 * Created by trustme on 2017/1/15.
 * index.js
 */
//删除string
function deleteString(key){

}

$(function () {
    //加载数据标记
    var loadDataFlag = true;
    //全部all
    $(".redisAll").on("click", function () {
        redisAll();
    });
    //string
    $("#string").on("click", function () {
        $("#side-menu").find("li a").removeClass("active");
        $(this).addClass("active");
        if (loadDataFlag) {
            loadDataFlag = false;
            $.ajax({
                url: ctx + "/string",
                type: "post",
                dataType: "json",
                success: function (data) {
                    $("#redisContent").empty();
                    showData(data, "1");
                    loadDataFlag = true;
                }
            });
        }
    });
    //list
    $("#list").on("click", function () {
        $("#side-menu").find("li a").removeClass("active");
        $(this).addClass("active");
        if (loadDataFlag) {
            loadDataFlag = false;
            $.ajax({
                url: ctx + "/list",
                type: "post",
                dataType: "json",
                success: function (data) {
                    $("#redisContent").empty();
                    showData(data, "2");
                    loadDataFlag = true;
                }
            });
        }
    });
    //set
    $("#set").on("click", function () {
        $("#side-menu").find("li a").removeClass("active");
        $(this).addClass("active");
        if (loadDataFlag) {
            loadDataFlag = false;
            $.ajax({
                url: ctx + "/set",
                type: "post",
                dataType: "json",
                success: function (data) {
                    $("#redisContent").empty();
                    showData(data, "3");
                    loadDataFlag = true;
                }
            });
        }
    });
    //zSet
    $("#zSet").on("click", function () {
        $("#side-menu").find("li a").removeClass("active");
        $(this).addClass("active");
        if (loadDataFlag) {
            loadDataFlag = false;
            $.ajax({
                url: ctx + "/zSet",
                type: "post",
                dataType: "json",
                success: function (data) {
                    $("#redisContent").empty();
                    showData(data, "4");
                    loadDataFlag = true;
                }
            });
        }
    });
    //hash
    $("#hash").on("click", function () {
        $("#side-menu").find("li a").removeClass("active");
        $(this).addClass("active");
        if (loadDataFlag) {
            loadDataFlag = false;
            $.ajax({
                url: ctx + "/hash",
                type: "post",
                dataType: "json",
                success: function (data) {
                    $("#redisContent").empty();
                    showData(data, "5");
                    loadDataFlag = true;
                }
            });
        }
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
    //提示框关闭
    $("#prompt").on("hide.bs.modal", function () {
        redisAll();
    });


    /**
     * 显示数据
     * @param data 数据
     * @param type 类型 1:string,2:list,3:set,4:zSet,5:hash
     */
    function showData(data, type) {
        if (data == null) {
            return;
        }
        var str;
        if (type == "1") {
            str = '<div class="panel panel-default">' +
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
            for (var key in data) {
                str += '<tr>' +
                    '<td>' + key + '</td>' +
                    '<td>' + data[key] + '</td>' +
                    '<td>' +
                    '<a href="#" class="btn btn-primary btn-xs" data-toggle="modal" data-target="#customerEditDialog" >修改</a> ' +
                    '<a href="#" class="btn btn-danger btn-xs" >删除</a>' +
                    '</td></tr>';
            }
            str += '</tbody> </table></div>';
        } else if (type == "2") {
            str = '<div class="panel panel-default">' +
                '<div class="panel-heading">list</div>' +
                '<table class="table table-bordered table-striped" style="border-bottom: 1px solid #ddd">' +
                '<thead>' +
                '<tr style=" border-top:1px solid #ddd;">' +
                '<th>key</th>' +
                '<th>values</th>' +
                '<th style="width:95px">操作</th>' +
                '</tr>' +
                '</thead>' +
                '<tbody>';
            for (var key in data) {
                str += '<tr>';
                str += '<td rowspan="' + data[key].length + '">' + key + '</td>';
                for (var i = 0, len = data[key].length; i < len; i++) {
                    str += '<td>' + data[key][i] + '</td>' +
                        '<td>' +
                        '<a href="#" class="btn btn-primary btn-xs" data-toggle="modal" data-target="#customerEditDialog" >修改</a> ' +
                        '<a href="#" class="btn btn-danger btn-xs" >删除</a>' +
                        '</td></tr>';
                }
            }
            str += '</tbody></table></div>';
        } else if (type == "3") {
            str = '<div class="panel panel-default">' +
                '<div class="panel-heading">set</div>' +
                '<table class="table table-bordered table-striped" style="border-bottom: 1px solid #ddd">' +
                '<thead>' +
                '<tr style=" border-top:1px solid #ddd;">' +
                '<th>key</th>' +
                '<th>values</th>' +
                '<th style="width:95px">操作</th>' +
                '</tr>' +
                '</thead>' +
                '<tbody>';
            for (var key in data) {
                str += '<tr>';
                str += '<td rowspan="' + data[key].length + '">' + key + '</td>';
                for (var i = 0, len = data[key].length; i < len; i++) {
                    str += '<td>' + data[key][i] + '</td>' +
                        '<td>' +
                        '<a href="#" class="btn btn-primary btn-xs" data-toggle="modal" data-target="#customerEditDialog" >修改</a> ' +
                        '<a href="#" class="btn btn-danger btn-xs" >删除</a>' +
                        '</td></tr>';
                }
            }
            str += '</tbody></table></div>';
        } else if (type == "4") {
            str = '<div class="panel panel-default">' +
                '<div class="panel-heading">zSet</div>' +
                '<table class="table table-bordered table-striped" style="border-bottom: 1px solid #ddd">' +
                '<thead>' +
                '<tr style=" border-top:1px solid #ddd;">' +
                '<th>key</th>' +
                '<th>score</th>' +
                '<th>element</th>' +
                '<th style="width:95px">操作</th>' +
                '</tr>' +
                '</thead>' +
                '<tbody>';
            for (var key in data) {
                str += '<tr>';
                str += '<td rowspan="' + data[key].length + '">' + key + '</td>';
                for (var i = 0, len = data[key].length; i < len; i++) {
                    str += '<td>' + data[key][i].score + '</td>' +
                        '<td>' + data[key][i].element + '</td>' +
                        '<td>' +
                        '<a href="#" class="btn btn-primary btn-xs" data-toggle="modal" data-target="#customerEditDialog" >修改</a> ' +
                        '<a href="#" class="btn btn-danger btn-xs" >删除</a>' +
                        '</td></tr>';
                }
            }
            str += '</tbody></table></div>';
        } else if (type == "5") {
            str = '<div class="panel panel-default">' +
                '<div class="panel-heading">hash</div>' +
                '<table class="table table-bordered table-striped" style="border-bottom: 1px solid #ddd">' +
                '<thead>' +
                '<tr style=" border-top:1px solid #ddd;">' +
                '<th>key</th>' +
                '<th>field</th>' +
                '<th>value</th>' +
                '<th style="width:95px">操作</th>' +
                '</tr>' +
                '</thead>' +
                '<tbody>';
            for (var key in data) {
                str += '<tr>';
                str += '<td rowspan="' + Object.getOwnPropertyNames(data[key]).length + '">' + key + '</td>';
                for (var field in data[key]) {
                    str += '<td>' + field + '</td>' +
                        '<td>' + data[key][field] + '</td>' +
                        '<td>' +
                        '<a href="#" class="btn btn-primary btn-xs" data-toggle="modal"  data-target="#customerEditDialog" >修改</a> ' +
                        '<a href="#" class="btn btn-danger btn-xs" >删除</a>' +
                        '</td></tr>';
                }
            }
            str += '</tbody></table></div>';
        }
        $("#redisContent").append(str);
    }

    /**
     * 加载全部数据
     */
    function redisAll() {
        $("#side-menu").find("li a").removeClass("active");
        $(".redisAll").addClass("active");
        if (loadDataFlag) {
            loadDataFlag = false;
            $.ajax({
                url: ctx + "/string",
                type: "post",
                dataType: "json",
                success: function (data) {
                    $("#redisContent").empty();
                    showData(data, "1");
                    $.ajax({
                        url: ctx + "/list",
                        type: "post",
                        dataType: "json",
                        success: function (data) {
                            showData(data, "2");
                            $.ajax({
                                url: ctx + "/set",
                                type: "post",
                                dataType: "json",
                                success: function (data) {
                                    showData(data, "3");
                                    $.ajax({
                                        url: ctx + "/zSet",
                                        type: "post",
                                        dataType: "json",
                                        success: function (data) {
                                            showData(data, "4");
                                            $.ajax({
                                                url: ctx + "/hash",
                                                type: "post",
                                                dataType: "json",
                                                success: function (data) {
                                                    showData(data, "5");
                                                    loadDataFlag = true;
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });
        }
    }
});