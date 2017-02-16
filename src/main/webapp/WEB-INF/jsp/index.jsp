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
<%--<div class="navbar-left sidebar" role="navigation" style="width: 121px;margin-top: 0px;">
    <div class="sidebar-nav navbar-collapse">
        <ul class="nav" id="side-menu">
            <li><a href="javascript:void(0);" class="redisAll active" ><i
                    class="fa fa-fw"></i>全部</a></li>
            <li><a href="javascript:void(0);" id="string"><i
                    class="fa fa-fw"></i> string</a></li>
            <li><a href="javascript:void(0);" id="list"><i
                    class="fa  fa-fw"></i> list</a></li>
            <li><a href="javascript:void(0);" id="set"><i
                    class="fa  fa-fw"></i> set</a></li>
            <li><a href="javascript:void(0);" id="zSet"><i
                    class="fa  fa-fw"></i> zSet</a></li>
            <li><a href="javascript:void(0);" id="hash"><i
                    class="fa  fa-fw"></i> hash</a></li>
        </ul>
    </div>
    <!-- /.sidebar-collapse -->
</div>--%>
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
                <p>删除成功</p>
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
                    <input type="text" class="form-control" id="parrent" value="sdfsdf" name="parrent">
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
    <div class="row">
        <div class="col-lg-12" id="redisContent">
            <!-- string-->
            <c:if test="${fn:length(string)>0}">
                <div class="panel panel-default">
                    <div class="panel-heading">string</div>
                    <!-- /.panel-heading -->
                    <table class="table table-bordered table-striped" style="border-bottom: 1px solid #ddd">
                        <thead>
                        <tr style=" border-top: 1px solid #ddd;">
                            <th>key</th>
                            <th>value</th>
                            <th style="width: 95px">操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${string}" var="str">
                            <tr>
                                <td>${str.key}</td>
                                <td>${str.value}</td>
                                <td>
                                    <a href="#" class="btn btn-primary btn-xs" data-toggle="modal"
                                       data-target="#customerEditDialog"
                                       onclick="editCustomer(${row.cust_id})">修改</a>
                                    <a href="#" class="btn btn-danger btn-xs"
                                       onclick="deleteString(${str.key});">删除</a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                    <!-- /.panel-body -->
                </div>
            </c:if>

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

            <div class="col-md-12 text-right">
                <itcast:page url="${pageContext.request.contextPath }/customer/index.action"/>
            </div>
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
<script src="${ctx}/js/bootstrap-treeview.js"></script>
<script src="${ctx}/js/redis/index.js"></script>
<script src="${ctx}/js/redis/whe-tree.js"></script>
<script type="text/javascript">
    $(function () {
        var flag=true;
       /* $('#tree').on('click', '.node-tree', function (event, data) {
            // 事件代码...
            var nodeId = $(this).attr("data-nodeid");
            console.log(nodeId)
            var text = $(this).find(".text").html().toUpperCase();
            var indexOf = text.indexOf("DB");
            if (indexOf != -1) {
                var dbSize = $(this).find(".badge").html();
                var db = text.substr(indexOf + 3);
                if (dbSize >0&&flag ) {
                    flag=false;
                    $.ajax({
                        url: ctx + "/scan",
                        data: {db: db},
                        type: "post",
                        dataType: "json",
                        success: function (data) {
                            alert(data.length)
                            $(this).siblings(".list-group-item[parent-id='" + nodeId + "']").remove();
                          $(this).addNode(nodeId,data);
                        }
                    })
                }
            }

        });*/
        var defaultData = ${tree};
        $("#tree").initTree(defaultData);
    })

</script>
</html>
