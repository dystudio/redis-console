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
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">Server<span class="caret"></span></a>
                    <ul class="dropdown-menu">
                        <li><a href="${ctx}/standalone/index">Standalone</a></li>
                        <li><a href="${ctx}/cluster/index">Cluster</a></li>
                    </ul>
                </li>
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
    $("#tree").initTree(defaultData);
</script>
</html>
