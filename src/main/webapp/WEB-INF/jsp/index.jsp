<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:if test="${ctx == null}">
    <c:set var="ctx" value="${pageContext.request.contextPath}" scope="application"/>
</c:if>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta charset="utf-8">
    <!--浏览器兼容，兼容IE-->
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <!--视口：响应式-->
    <meta name="viewport"
          content="width=device-width, initial-scale=2,minimum-scale=1.5,maximum-scale=3,user-scalable=yes"/>
    <!-- 上述3个meta标签*必须*放在最前面，任何其他内容都*必须*跟随其后！ -->
    <title>Redis控制台</title>
    <!-- 1.导入css -->
    <link href="${ctx}/css/bootstrap.css" rel="stylesheet">
    <link href="${ctx}/css/index.css" rel="stylesheet">

    <!--兼容ie6、7、8的浏览器，他们都不支持html5-->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
    <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

    <script type="text/javascript">
        var ctx = "${ctx}";
    </script>


</head>

<body>
<div class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div style="margin-left: 100px;">
        <div class="navbar-header">
            <a class="navbar-brand redisAll" href="javascript:void(0);">Redis控制台</a>
        </div>
        <div class="navbar-collapse collapse">
            <ul class="nav navbar-nav">
                <li class="active redisAll"><a href="javascript:void(0);">Data</a></li>
                <li><a href="javascript:void(0);">Info</a></li>
                <li><a href="javascript:void(0);">Config</a></li>
                <li class="dropdown ">
                    <a href="javascript:void(0);">Offset</a>
                </li>
            </ul>
        </div>
    </div>
</div>
<div style="height: 60px"></div>
<div id="tree"></div>
<!--模态框-->
<!--全部删除提示-->
<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title">删除提示</h4>
            </div>
            <div class="modal-body">
                <p>确认全部删除数据吗?</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
                <button type="button" class="btn btn-danger" id="flushAll">确认</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<div class="modal fade" id="prompt" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title" id="promptTitle">成功提示</h4>
            </div>
            <div class="modal-body" id="promptContent">
                <p>操作成功</p>
            </div>
            <div class="modal-footer">
                <button type="button" id="promptBtn" class="btn btn-success" data-dismiss="modal">确认</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->


<div class="container theme-showcase">
    <!-- /.row -->
    <div class="panel panel-default">
        <div class="panel-body">
            <button type="button" id="backup" class="btn btn-primary navbar-btn">备份</button>
            <span class="btn btn-success btn-file"> 恢复
                <span class="glyphicon" aria-hidden="true"></span>
                <form id="fileForm" method="post" enctype="multipart/form-data">
                    <input type="file" name="file" value="" id="recover"/>
                </form>
            </span>
            <span class="btn btn-success btn-file"> JDK序列化恢复
                <span class="glyphicon" aria-hidden="true"></span>
                <form id="serializeFileForm" method="post" enctype="multipart/form-data">
                    <input type="file" name="file" value="" id="serializeRecover"/>
                </form>
            </span>
            <button type="button" class="btn btn-danger" data-toggle="modal" data-target="#myModal">全部删除</button>
            <form class="form-inline" action="${ctx}/index"
                  method="post" style="display: inline">
                <div class="form-group">
                    <label>key</label>
                    <input type="text" class="form-control" id="parrent" value="" name="parrent">
                </div>
                <div class="form-group">
                    <label for="customerFrom">类型</label>
                    <select class="form-control" id="customerFrom" name="custSource">
                        <option value="">--请选择--</option>
                        <c:forEach items="${type}" var="item">
                            <option value="${item}">${item}</option>
                        </c:forEach>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary">查询</button>
            </form>
        </div>
    </div>
    <div id="redisContent">
        <ul class="nav nav-tabs">
            <li role="presentation" class="active"><a href="javascript:void(0);">list</a></li>
            <li role="presentation"><a href="javascript:void(0);">生存时间</a></li>
        </ul>
        <div class="panel panel-default" id="type-content">
            <table class="table table-bordered table-hover ">
                <thead>
                <tr>
                    <th style="width: 87%;">key</th>
                    <th style="text-align: center;">操作</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td style="padding: 0;">
                        <input type="text" class="form-control" disabled value="asdas">
                    </td>
                    <td>
                        <a href="javascript:void(0);" class="btn btn-primary btn-xs"
                           onclick="removeDisabled(event)">修改</a>
                        <button type="button" class="btn btn-success btn-xs disabled">保存</button>
                        <a href="javascript:void(0);" class="btn btn-danger btn-xs"
                           onclick="deleteString(${str.key});">删除</a>
                    </td>
                </tr>
                </tbody>
                <thead>
                <tr>
                    <th>values</th>
                    <th style="text-align: center;">操作</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td style="padding: 0;">
                        <input type="text" class="form-control" value="value">
                    </td>
                    <td>
                        <button type="button" class="btn btn-success btn-xs " onclick="updateString(this)">保存</button>
                        <a href="javascript:void(0);" class="btn btn-danger btn-xs"
                           onclick="deleteString(${str.key});">删除</a>
                    </td>
                </tr>
                <tr>
                    <td style="padding: 0;">
                        <input type="text" class="form-control" value="value">
                    </td>
                    <td>
                        <button type="button" class="btn btn-success btn-xs " onclick="updateString(this)">保存</button>
                        <a href="javascript:void(0);" class="btn btn-danger btn-xs"
                           onclick="deleteString(${str.key});">删除</a>
                    </td>
                </tr>
                <tr>
                    <td style="padding: 0;">
                        <input type="text" class="form-control" value="value">
                    </td>
                    <td>
                        <button type="button" class="btn btn-success btn-xs " onclick="updateString(this)">保存</button>
                        <a href="javascript:void(0);" class="btn btn-danger btn-xs"
                           onclick="deleteString(${str.key});">删除</a>
                    </td>
                </tr>
                <tr>
                    <td style="padding: 0;">
                        <input type="text" class="form-control" value="value">
                    </td>
                    <td>
                        <button type="button" class="btn btn-success btn-xs " onclick="updateString(this)">保存</button>
                        <a href="javascript:void(0);" class="btn btn-danger btn-xs"
                           onclick="deleteString(${str.key});">删除</a>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="panel panel-default" style="display: none;" id="ttl-content">
            <table class="table table-bordered ">
                <thead>
                <tr>
                    <th style="width: 91%;">过期时间(秒)</th>
                    <th style="text-align: center;">操作</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td style="padding: 0;">
                        <input type="text" class="form-control" disabled
                               value="" size="5" style="ime-mode:disabled"
                               onkeyup="checkNumber(this)" onafterpaste="checkNumber(this)"/>
                    </td>
                    <td>
                        <a href="#" class="btn btn-primary btn-xs"
                           onclick="removeDisabled(event)">修改</a>
                        <button type="button" class="btn btn-success btn-xs disabled">保存</button>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <div class="row">
        <div class="col-lg-12">

            <!-- list-->
            <c:if test="${fn:length(list)>0}">
                <div class="panel ">
                    <div class="panel-heading">list</div>
                    <!-- /.panel-heading -->
                    <table class="table table-bordered table-striped">
                        <thead>
                        <tr style=" border-top: 1px solid #ddd;">
                            <th>key</th>
                            <th>values</th>
                            <th style="width: 95px">操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${list}" var="list">
                            <c:forEach items="${list.value}" var="val" varStatus="vs">
                                <tr>
                                    <c:if test="${vs.index==0}">
                                        <td rowspan="${fn:length(list.value)}">${list.key}</td>
                                    </c:if>
                                    <td>${val}</td>
                                    <td>
                                        <a href="#" class="btn btn-primary btn-xs" data-toggle="modal"
                                           data-target="#customerEditDialog"
                                           onclick="editCustomer(${row.cust_id})">修改</a>
                                        <a href="#" class="btn btn-danger btn-xs"
                                           onclick="deleteCustomer(${row.cust_id})">删除</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:forEach>
                        </tbody>
                    </table>
                    <!-- /.panel-body -->
                </div>
            </c:if>

            <!-- set-->
            <c:if test="${fn:length(set)>0}">
                <div class="panel ">
                    <div class="panel-heading">set</div>
                    <!-- /.panel-heading -->
                    <table class="table table-bordered table-striped">
                        <thead>
                        <tr style=" border-top: 1px solid #ddd;">
                            <th>key</th>
                            <th>values</th>
                            <th style="width: 95px">操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${set}" var="set">
                            <c:forEach items="${set.value}" var="val" varStatus="vs">
                                <tr>
                                    <c:if test="${vs.index==0}">
                                        <td rowspan="${fn:length(set.value)}">${set.key}</td>
                                    </c:if>
                                    <td>${val}</td>
                                    <td>
                                        <a href="#" class="btn btn-primary btn-xs" data-toggle="modal"
                                           data-target="#customerEditDialog"
                                           onclick="editCustomer(${row.cust_id})">修改</a>
                                        <a href="#" class="btn btn-danger btn-xs"
                                           onclick="deleteCustomer(${row.cust_id})">删除</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:forEach>
                        </tbody>
                    </table>
                    <!-- /.panel-body -->
                </div>
            </c:if>
            <!-- zset-->
            <c:if test="${fn:length(zSet)>0}">
                <div class="panel ">
                    <div class="panel-heading">zSet</div>
                    <!-- /.panel-heading -->
                    <table class="table table-bordered table-striped">
                        <thead>
                        <tr style=" border-top: 1px solid #ddd;">
                            <th>key</th>
                            <th>score</th>
                            <th>element</th>
                            <th style="width: 95px">操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${zSet}" var="zSet">
                            <c:forEach items="${zSet.value}" var="tuple" varStatus="vs">
                                <tr>
                                    <c:if test="${vs.index==0}">
                                        <td rowspan="${fn:length(zSet.value)}">${zSet.key}</td>
                                    </c:if>
                                    <td>${tuple.score}</td>
                                    <td>${tuple.element}</td>
                                    <td>
                                        <a href="#" class="btn btn-primary btn-xs"
                                           onclick="editCustomer(${row.cust_id})">修改</a>
                                        <a href="#" class="btn btn-danger btn-xs"
                                           onclick="deleteCustomer(${row.cust_id})">删除</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:forEach>
                        </tbody>
                    </table>
                    <!-- /.panel-body -->
                </div>
            </c:if>
            <!--hash-->
            <c:if test="${fn:length(hash)>0}">
                <div class="panel ">
                    <div class="panel-heading">hash</div>
                    <!-- /.panel-heading -->
                    <table class="table table-bordered table-striped">
                        <thead>
                        <tr style=" border-top: 1px solid #ddd;">
                            <th>key</th>
                            <th>field</th>
                            <th>value</th>
                            <th style="width: 95px">操作</th>

                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${hash}" var="hash">
                            <c:forEach items="${hash.value}" var="map" varStatus="vs">
                                <tr>
                                    <c:if test="${vs.index==0}">
                                        <td rowspan="${fn:length(hash.value)}"
                                            style="width: 30px !important;">${hash.key}</td>
                                    </c:if>
                                    <td style="width: 30px !important;">${map.key}</td>
                                    <td>${map.value}</td>
                                    <td>
                                        <a href="#" class="btn btn-primary btn-xs" data-toggle="modal"
                                           data-target="#customerEditDialog"
                                           onclick="editCustomer(${row.cust_id})">修改</a>
                                        <a href="#" class="btn btn-danger btn-xs"
                                           onclick="deleteCustomer(${row.cust_id})">删除</a>
                                    </td>
                                </tr>
                            </c:forEach>

                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:if>
            <!-- /.panel -->
        </div>
        <!-- /.col-lg-12 -->
    </div>
</div>
<!-- /#page-wrapper -->
</body>
<!-- 2 jQuery类库 -->
<script src="${ctx}/js/jquery.min.js"></script>
<!-- 3 bootstrap 类库 -->
<script src="${ctx}/js/bootstrap.min.js"></script>
<script src="${ctx}/js/redis/index.js"></script>
<script src="${ctx}/js/redis/whe-tree.js"></script>
<script type="text/javascript">
    //当前key
    var key;
    //当前数据类型
    var redisType;
    //当前是第几个数据库
    var redisDb;
    var string = "string";
    var list = "list";
    var set = "set";
    var zset = "zset";
    var hash = "hash";
    var nowNodeId;
    var listSize = 0;

    var ttlStr = '<div class="panel " style="display: none;" id="ttl-content"> <table class="table table-bordered ">' +
        '<thead><tr><th style="width: 87%;">过期时间(秒)</th><th style="text-align: center;">操作</th></tr></thead>' +
        '<tbody><tr><td style="padding: 0;"><input type="text" maxlength="10" class="form-control" disabled value="-1"  ' +
        'onkeyup="checkNumber(this)"/></td><td><a href="javascript:void(0);" class="btn btn-primary btn-xs"' +
        'onclick="removeDisabled(event)">修改</a><button type="button" class="btn btn-success btn-xs disabled" style="margin-left: 5px;" onclick="setExpire(this)">保存</button>' +
        '</td></tr></tbody></table></div>';
    /**
     * 重命名key
     **/
    function rename(th) {
        var newKey = $(th).closest("tr").find("input").val();
        if (newKey.trim() == '' || newKey == key) {
            return;
        }
        $.ajax({
            url: ctx + "/renameNx",
            data: {db: redisDb, oldKey: key, newKey: newKey},
            type: "post",
            dataType: "json",
            success: function (data) {
                if (data == "1") {
                    key = newKey;
                    $("li[node-id='" + nowNodeId + "']").find(".text").html(key);
                }
                showModel(data);
            }
        });
    }
    //更新生存时间
    function setExpire(th) {
        var seconds = $(th).closest("tr").find("input").val();
        if (!isNaN(seconds)) {
            if (2147483648 < seconds) {
                alert("超过int最大范围!");
                return;
            }
            $.ajax({
                url: ctx + "/setExpire",
                data: {db: redisDb, key: key, seconds: seconds},
                type: "post",
                dataType: "json",
                success: function (data) {
                    showModel(data);
                }
            });
        }
    }
    //删除key
    function delKey(th) {
        $.ajax({
            url: ctx + "/delKey",
            data: {db: redisDb, key: key},
            type: "post",
            dataType: "json",
            success: function (data) {
                showModel(data);
                if (data == "1") {
                    $("li[node-id='" + nowNodeId + "']").empty();
                    $("#redisContent").empty();
                }
            }
        });
    }
    /**
     * string更新值
     * */
    function updateString(th) {
        var val = $(th).closest("tr").find("input").val();
        if (val.trim() == '') {
            return;
        }
        $.ajax({
            url: ctx + "/updateString",
            data: {db: redisDb, key: key, val: val},
            type: "post",
            dataType: "json",
            success: function (data) {
                showModel(data);
            }
        });
    }
    function updateList(th) {
        var val = $(th).closest("tr").find("input").val();
        var index = $(th).closest("tr").find("td").first().html();
        --index;
        $.ajax({
            url: ctx + "/updateList",
            data: {db: redisDb, index: index, key: key, val: val},
            type: "post",
            dataType: "json",
            success: function (data) {
                showModel(data);
            }
        });
    }
    function delList(th) {
        var index = $(th).closest("tr").find("td").first().html();
        --index;
        var len = $(th).closest("tr").prevAll().length;
        var strLen = len.toString();
        var strIndex = index.toString();
        len = parseInt(strIndex.substring(strIndex.length - strLen.length)) - len;
        index = index - len;
        alert(index);
        $.ajax({
            url: ctx + "/delList",
            data: {db: redisDb, index: index, listSize: listSize, key: key},
            type: "post",
            dataType: "json",
            success: function (data) {
                if (data == "2") {
                    alert("不能从list删除行,因为行已经改变了。重载值并再试一次");
                    return;
                }
                $(th).closest("tr").remove();
                --listSize;
                showModel(data);
            }
        });
    }

    function updateSet(th) {
        var node = $(th).closest("tr").find("input");
        var val = node.val();
        var oldVal = node.attr("oldVal");
        $.ajax({
            url: ctx + "/updateSet",
            data: {db: redisDb, key: key, oldVal: oldVal, newVal: val},
            type: "post",
            dataType: "json",
            success: function (data) {
                showModel(data);
            }
        });
    }
    function delSet(th) {
        var val = $(th).closest("tr").find("input").attr("oldVal");
        $.ajax({
            url: ctx + "/delSet",
            data: {db: redisDb, key: key, val: val},
            type: "post",
            dataType: "json",
            success: function (data) {
                $(th).closest("tr").remove();
                showModel(data);
            }
        });
    }
    function removeDisabled(event) {
        event = event || window.event;
        var obj = event.srcElement ? event.srcElement : event.target;
        $(obj).next("button").removeClass("disabled");
        $(obj).closest("tr").find("input").removeAttr("disabled");
    }

    /**
     * 检查是否是数字不包含小数点 包含负数
     * @param th this
     */
    function checkNumber(th) {
        var value = th.value;
        if (value || value.length > 0) {
            var charAt = value.charAt(0);
            if (charAt == '-') {
                th.value = charAt + value.replace(/[^0-9]/g, '');
            } else {
                th.value = value.replace(/[^0-9]/g, '');
            }
        }
    }

    $(function () {
        //key导航切换
        $("#redisContent").on('click', ".nav-tabs li", function () {
            $(this).siblings("li").removeClass("active");
            $(this).addClass("active");
            var text = $(this).find("a").html();
            if (text == "生存时间") {
                if (!$(this).hasClass("firstClick")) {
                    $(this).addClass("firstClick");
                    $.ajax({
                        url: ctx + "/ttl",
                        data: {db: redisDb, key: key},
                        type: "post",
                        dataType: "json",
                        success: function (data) {
                            $("#ttl-content").find("input").val(data);
                        }
                    });
                }
                $("#type-content").css("display", "none");
                $("#ttl-content").css("display", "block");
            } else {
                $("#ttl-content").css("display", "none");
                $("#type-content").css("display", "block");
            }
        })
        var defaultData = ${tree};
        $("#tree").initTree(defaultData);
        $("#tree").on('click', '.node-div', function () {
            var $result = $(this).find(".expand-icon");
            if ($result.length > 0) {
                return;
            }
            var db = $(this).parent().closest(".child_ul").siblings(".node-div").find(".text").html().toUpperCase();
            var indexOf = db.indexOf("DB-");
            redisDb = db.substring(indexOf + 3);
            var text = $(this).find(".text");
            key = text.html();
            var type = text.attr("type");
            nowNodeId = $(this).closest("li").attr("node-id");
            redisType = type;
            if (type == string) {
                getString();
            } else if (type == list) {
                $.ajax({
                    url: ctx + "/getList",
                    data: {db: redisDb, key: key, pageNo: 1},
                    type: "post",
                    dataType: "json",
                    success: function (data) {
                        var str = '<ul class="nav nav-tabs"><li role="presentation" class="active"><a href="javascript:void(0);">list</a></li>' +
                            '<li role="presentation"><a href="javascript:void(0);">生存时间</a></li> </ul> <div class="panel" id="type-content">' +
                            '<table class="table table-bordered "><thead> <tr><th style="width: 87%;">key</th><th style="text-align: center">' +
                            '操作</th> </tr> </thead> <tbody style="border: 1px solid #ddd;"> <tr> <td style="padding: 0;"><input type="text" disabled class="form-control" ' +
                            'value="' + key + '"> </td><td> <a href="javascript:void(0);" class="btn btn-primary btn-xs" ' +
                            'onclick="removeDisabled(event)">修改</a> <button type="button" class="btn btn-success btn-xs disabled" onclick="rename(this)">保存</button>' +
                            '<a href="javascript:void(0);" class="btn btn-danger btn-xs" onclick="delKey(this);" style="margin-left: 4px;">删除</a>' +
                            '</td> </tr></tbody></table><table class="table table-bordered "><thead><tr><th style="width:3%;">row</th><th style="width:83%;">value</th><th style="text-align: center;">操作</th></tr></thead><tbody id="list-content">';
                        for (var i = 0; i < data.results.length; i++) {
                            str += '<tr><td >' + ((data.pageNo - 1) * data.pageSize + i + 1) + '</td><td style="padding: 0;"><input type="text" class="form-control" value="' + data.results[i] + '"></td>' +
                                '<td><button type="button" class="btn btn-success btn-xs " onclick="updateList(this)">保存</button>' +
                                '<a href="javascript:void(0);" class="btn btn-danger btn-xs" onclick="delList(this);" style="margin-left: 4px;">删除</a></td></tr>';
                        }
                        str += '</table><div id="page">';
                        for (var j = 0; j < data.pageView.length; j++) {
                            str += data.pageView[j];
                        }
                        str += '</div></div>' + ttlStr;
                        listSize = data.totalRecord;
                        $("#redisContent").html(str);
                    }
                });
            } else if (type == set) {
                getSet();
            }

        })

    });
    function pageViewAjax(url, th) {
        $.ajax({
            url: url,
            data: {db: redisDb, key: key},
            type: "post",
            dataType: "json",
            success: function (data) {
                var str = "";
                for (var i = 0; i < data.results.length; i++) {
                    str += '<tr><td>' + ((data.pageNo - 1) * data.pageSize + i + 1) + '</td><td style="padding: 0;"><input type="text" class="form-control" value="' + data.results[i] + '"></td>' +
                        '<td><button type="button" class="btn btn-success btn-xs " onclick="updateList(this)">保存</button>' +
                        '<a href="javascript:void(0);" class="btn btn-danger btn-xs" onclick="delList(this);" style="margin-left: 4px;">删除</a></td></tr>';
                }
                $("#list-content").html(str);
                var page = "";
                for (var j = 0; j < data.pageView.length; j++) {
                    page += data.pageView[j];
                }
                listSize = data.totalRecord;
                $("#page").html(page);
            }
        });
    }
    function getSet() {
        $.ajax({
            url: ctx + "/getSet",
            data: {db: redisDb, key: key},
            type: "post",
            dataType: "json",
            success: function (data) {
                var str = '<ul class="nav nav-tabs"><li role="presentation" class="active"><a href="javascript:void(0);">set</a></li>' +
                    '<li role="presentation"><a href="javascript:void(0);">生存时间</a></li> </ul> <div class="panel" id="type-content">' +
                    '<table class="table table-bordered "><thead><tr><th style="width: 87%;">key</th><th style="text-align: center">' +
                    '操作</th></tr></thead> <tbody style="border:1px solid #ddd;"><tr><td style="padding: 0;"><input type="text" disabled class="form-control" ' +
                    'value="' + key + '"> </td><td> <a href="javascript:void(0);" class="btn btn-primary btn-xs" ' +
                    'onclick="removeDisabled(event)">修改</a> <button type="button" class="btn btn-success btn-xs disabled" onclick="rename(this)">保存</button>' +
                    '<a href="javascript:void(0);" class="btn btn-danger btn-xs" onclick="delKey(this);" style="margin-left: 4px;">删除</a>' +
                    '</td> </tr></tbody></table><table class="table table-bordered "><thead><tr><th style="width:87%;">value</th><th style="text-align: center;">操作</th></tr></thead><tbody id="list-content">';
                for (var i = 0; i < data.length; i++) {
                    str += '<tr><td style="padding: 0;"><input type="text" class="form-control" oldVal="' + data[i] + '" value="' + data[i] + '"></td>' +
                        '<td><button type="button" class="btn btn-success btn-xs " onclick="updateSet(this)">保存</button>' +
                        '<a href="javascript:void(0);" class="btn btn-danger btn-xs" onclick="delSet(this);" style="margin-left: 4px;">删除</a></td></tr>';
                }
                str += '</table></div>' + ttlStr;
                $("#redisContent").html(str);
            }
        });
    }
    function getString() {
        $.ajax({
            url: ctx + "/getString",
            data: {db: redisDb, key: key},
            type: "post",
            dataType: "text",
            success: function (data) {
                var str = '<ul class="nav nav-tabs"><li role="presentation" class="active"><a href="javascript:void(0);">string</a></li>' +
                    '<li role="presentation"><a href="javascript:void(0);">生存时间</a></li> </ul> <div class="panel panel-default" id="type-content">' +
                    '<table class="table table-bordered "><thead> <tr><th style="width: 87%;">key</th><th style="text-align: center">' +
                    '操作</th> </tr> </thead> <tbody> <tr> <td style="padding: 0;"><input type="text" disabled class="form-control" ' +
                    'value="' + key + '"> </td><td> <a href="javascript:void(0);" class="btn btn-primary btn-xs" ' +
                    'onclick="removeDisabled(event)">修改</a> <button type="button" class="btn btn-success btn-xs disabled" onclick="rename(this)">保存</button>' +
                    '<a href="javascript:void(0);" class="btn btn-danger btn-xs" onclick="delKey(this);" style="margin-left: 4px;">删除</a>' +
                    '</td> </tr></tbody><thead><tr><th>value</th><th style="text-align: center;">操作</th></tr></thead>' +
                    '<tbody><tr><td style="padding: 0;"><input type="text" class="form-control" value="' + data + '"></td>' +
                    '<td><button type="button" class="btn btn-success btn-xs " onclick="updateString(this)">保存</button></td></tr></table>' +
                    '</table> </div>' + ttlStr;
                $("#redisContent").html(str);
            }
        });
    }

    function nextPage(db, cursor, event) {
        event = event || window.event;
        var obj = event.srcElement ? event.srcElement : event.target;
        $.ajax({
            url: ctx + "/nextPage",
            data: {db: db, cursor: cursor},
            type: "post",
            dataType: "text",
            success: function (data) {
                data = eval('(' + data + ')');
                $(obj).addNode(data)
            }
        })
    }

    function upPage(db, cursor, event) {
        event = event || window.event;
        var obj = event.srcElement ? event.srcElement : event.target;
        $.ajax({
            url: ctx + "/upPage",
            data: {db: db, cursor: cursor},
            type: "post",
            dataType: "text",
            success: function (data) {
                data = eval('(' + data + ')');
                $(obj).addNode(data)
            }
        })
    }

    function showModel(data) {
        if (data == '1') {
            $("#promptTitle").html("成功提示");
            $("#promptContent").html("<p>修改成功</p>");
            $("#promptBtn").removeClass("btn-danger").addClass("btn-success");
        } else if (data == "2") {
            $("#promptTitle").html("失败提示");
            $("#promptContent").html("<p>键已存在!</p>");
            $("#promptBtn").removeClass("btn-success").addClass("btn-danger");
        } else {
            $("#promptTitle").html("失败提示");
            $("#promptContent").html("<p>修改失败</p>");
            $("#promptBtn").removeClass("btn-success").addClass("btn-danger");
        }
        $("#prompt").modal("show");

    }
</script>
</html>
