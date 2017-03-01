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
                    <a href="#" class="dropdown-toggle"
                       data-toggle="dropdown">${server=="/cluster"?"Cluster":"Standalone"}<span
                            class="caret"></span></a>
                    <ul class="dropdown-menu">
                        <li><a href="${ctx}/standalone/index">Standalone</a></li>
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
            <button type="button" id="addRedis" class="btn btn-success navbar-btn">添加</button>
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
            <form class="form-inline" action="${ctx}${server}/index"
                  method="post" style="display: inline">
                <div class="form-group">
                    <label>key</label>
                    <input type="text" class="form-control" id="match" value="${match}" name="match">
                </div>
                <%--  <div class="form-group">
                      <label for="customerFrom">类型</label>
                      <select class="form-control" id="customerFrom" name="custSource">
                          <option value="">--请选择--</option>
                          <c:forEach items="${type}" var="item">
                              <option value="${item}">${item}</option>
                          </c:forEach>
                      </select>
                  </div>--%>
                <button type="submit" class="btn btn-primary">查询</button>
            </form>
        </div>
    </div>
    <div id="redisContent">
    </div>
</div>
<!-- 客户编辑对话框 -->
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
                <form class="form-horizontal" id="add_redis_form">
                    <div class="form-group">
                        <label for="redis_key" class="col-sm-2 control-label">key</label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="redis_key" placeholder="key"
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
                            <textarea class="form-control" id="redis_field"
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
                <button type="button" class="btn btn-success" data-dismiss="modal" id="redis_save">保存</button>
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
    $("#tree").initTree(defaultData);
    $("#addRedis").on('click', function () {
        $("#redis_add_dialog").modal("show");
    });
    $("#redis_type").on('change', function () {
        if (this.selectedIndex == 4) {
            $("#hash_field").css("display", "");
        } else if (this.selectedIndex == 3) {
            $("#zSet_score").css("display", "");
        } else {
            $("#hash_field").css("display", "none");
            $("#zSet_score").css("display", "none");
        }
    })
    ;
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

        $("#add_redis_form").ajaxSubmit(options);
    })
</script>
</html>
