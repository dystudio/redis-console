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
                <li class="active dropdown redisAll">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <c:choose>
                            <c:when test="${server=='/standalone'}">Standalone</c:when>
                            <c:when test="${server=='/sentinel'}">Sentinel</c:when>
                            <c:when test="${server=='/cluster'}">Cluster</c:when>
                            <c:otherwise>Standalone</c:otherwise>
                        </c:choose>
                        <span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu">
                        <li><a href="${ctx}/standalone/index">Standalone</a></li>
                        <li><a href="${ctx}/sentinel/index">Sentinel</a></li>
                        <li><a href="${ctx}/cluster/index">Cluster</a></li>
                    </ul>
                </li>
                <li><a href="javascript:void(0);" class="noExploit">Info</a></li>
                <li><a href="javascript:void(0);" class="noExploit">Config</a></li>
                <li class="dropdown ">
                    <a href="javascript:void(0);" class="noExploit">Offset</a>
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
            <div class="form-group" style="margin: 0px">
                <label class="col-sm-2 control-label" style="float:left;padding:16px 14px 0 0px;width: 43px">视图</label>
                <div class="col-sm-10" id="redis_view" style="width: 150px;margin-top: 7px">
                    <select class="form-control" name="redis_serializable">
                        <option value="0" selected>Plain Text</option>
                        <option value="1">JSON</option>
                        <option value="2">JDK序列化</option>
                        <option value="3">JDK序列化和JSON</option>
                    </select>
                </div>
            </div>
            <button type="button" id="addRedis" class="btn btn-success navbar-btn">添加</button>
            <button type="button" id="backup" class="btn btn-primary navbar-btn">备份</button>
            <span class="btn btn-success btn-file"> <span class="text">恢复</span>
                <span class="glyphicon" aria-hidden="true"></span>
                <form id="fileForm" method="post" enctype="multipart/form-data">
                    <input type="file" name="file" value="" id="recover"/>
                </form>
            </span>
            <span class="btn btn-success btn-file"> <span class="text">JDK序列化恢复</span>
                <span class="glyphicon" aria-hidden="true"></span>
                <form id="serializeFileForm" method="post" enctype="multipart/form-data">
                    <input type="file" name="file" value="" id="serializeRecover"/>
                </form>
            </span>
            <button type="button" class="btn btn-danger" data-toggle="modal" data-target="#myModal">全部删除</button>
            <form class="form-inline" action="${ctx}${server}/index"
                  method="post" style="display: inline">
                <div class="form-group">
                    <label>key</label>
                    <input type="text" class="form-control" id="match" value="${match}" name="match">
                </div>
                <button type="submit" class="btn btn-primary">查询</button>
            </form>
        </div>
    </div>
    <div id="redisContent">
    </div>
</div>

<div class="modal fade" id="redis_add_dialog" tabindex="-1" role="dialog"
     aria-labelledby="myModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="myModalLabel">添加数据</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal" id="add_redis_form" action="">
                    <div class="form-group">
                        <label for="redis_key" class="col-sm-2 control-label">key</label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="redis_key"
                                   name="redis_key">
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="redis_type" style="float:left;padding:10px 15px 0 27px;">数据类型</label>
                        <div class="col-sm-10">
                            <select class="form-control" id="redis_type" name="redis_type">
                                <option value="string" selected>string</option>
                                <option value="list">list</option>
                                <option value="set">set</option>
                                <option value="zset">zset</option>
                                <option value="hash">hash</option>
                            </select>
                        </div>
                    </div>
                    <c:if test="${server!='/cluster'}">
                        <div class="form-group">
                            <label for="redis_data_size" style="float:left;padding:10px 15px 0 40px;">数据库</label>
                            <div class="col-sm-10">
                                <select class="form-control" id="redis_data_size" name="redis_data_size">
                                    <c:if test="${not empty dataSize}">
                                    <c:forEach begin="0" end="${dataSize-1}" varStatus="vs">
                                        <option value="${vs.index}">db-${vs.index}</option>
                                    </c:forEach>
                                    </c:if>
                                </select>
                            </div>
                        </div>
                    </c:if>
                    <div class="form-group">
                        <label for="redis_serializable" style="float:left;padding:10px 14px 0 41px;">序列化</label>
                        <div class="col-sm-10">
                            <select class="form-control" id="redis_serializable" name="redis_serializable">
                                <option value="0" selected>否</option>
                                <option value="1">JDK序列化</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-group" id="zSet_score" style="display: none">
                        <label for="redis_score" class="col-sm-2 control-label">score</label>
                        <div class="col-sm-10">
                            <input type="text" maxlength="50" id="redis_score" name="redis_score" class="form-control"
                                   value="0" onkeyup="checkDouble(this)">
                        </div>
                    </div>
                    <div class="form-group" id="hash_field" style="display: none">
                        <label for="redis_field" class="col-sm-2 control-label">field</label>
                        <div class="col-sm-10">
                            <textarea class="form-control"  id="redis_field"
                                      name="redis_field"></textarea>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="redis_value" class="col-sm-2 control-label">value</label>
                        <div class="col-sm-10">
                            <textarea class="form-control" id="redis_value"
                                      name="redis_value"></textarea>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-success" id="redis_save">保存</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
            </div>
        </div>
    </div>
</div>
<!-- /#page-wrapper -->

</body>
<!-- 2 jQuery类库 -->
<script src="${ctx}/js/jquery.min.js"></script>
<script src="${ctx}/js/jquery.form.min.js"></script>

<!-- 3 bootstrap 类库 -->
<script src="${ctx}/js/bootstrap.min.js"></script>
<script src="${ctx}/js/redis/index.js"></script>
<script src="${ctx}/js/redis/whe-tree.js"></script>
<script type="text/javascript">
    var defaultData = ${tree};
    var server = '${server}';
    var match = '${match}';
    var redisView = 0;
    var serialize = "";
    $("#tree").initTree(defaultData);
    //添加
    $("#addRedis").on('click', function () {
        if (key != null && redisType != string) {
            $("#redis_key").val(key);
        }else{
            $("#redis_key").val("");
        }

        if (redisType != null) {
            $("#redis_type").find("option[value='" + redisType + "']").prop("selected", true);
        }
        if (redisDb != null) {
            $("#redis_data_size").find("option[value='" + redisDb + "']").prop("selected", true);
        }
        $("#redis_add_dialog").modal("show");
    });

    //添加数据类型切换
    $("#redis_type").on('change', function () {
        if (this.selectedIndex == 4) {
            $("#hash_field").css("display", "");
        } else if (this.selectedIndex == 3) {
            $("#zSet_score").css("display", "");
        } else {
            $("#hash_field").css("display", "none");
            $("#zSet_score").css("display", "none");
        }
    });

    //保存
    $("#redis_save").on('click', function () {
        var key = $("#redis_key").val().trim();
        var checkFlag = true;
        if (key == "") {
            $("#redis_key").closest(".form-group").addClass("has-error");
            checkFlag = false;
        } else {
            $("#redis_key").closest(".form-group").removeClass("has-error");
        }
        var type = $("#redis_type  option:selected").html();
        if (type == hash) {
            var field = $("#redis_field").val().trim();
            if (field == "") {
                $("#redis_field").closest(".form-group").addClass("has-error");
                checkFlag = false;
            } else {
                $("#redis_field").closest(".form-group").removeClass("has-error");
            }
        }
        var val = $("#redis_value").val().trim();
        if (val == "") {
            $("#redis_value").closest(".form-group").addClass("has-error");
            checkFlag = false;
        } else {
            $("#redis_value").closest(".form-group").removeClass("has-error");
        }
        if (!checkFlag) {
            return;
        }
        var options = {
            url: ctx + server + "/save",
            type: "post",
            dataType: "text",
            success: function (data) {
                if (data == "1") {
                    document.location.reload();//当前页面
                }
                showModel(data);
            }
        };
        $("#redis_add_dialog").modal('hide');
        $("#add_redis_form").ajaxSubmit(options);
    });

    //视图切换
    $("#redis_view").on('change', function () {
        redisView = $(this).find("option:selected").val();
        if (redisView == 3 || redisView == 2) {
            serialize = "/serialize";
        } else {
            serialize = "";
        }
        if (redisType == string) {
            getString();
        } else if (redisType == list) {
            getList();
        } else if (redisType == set) {
            getSet();
        } else if (redisType == zset) {
            getZSet();
        } else if (redisType == hash) {
            getHash();
        }
    })
</script>
</html>
