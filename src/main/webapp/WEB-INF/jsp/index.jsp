<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ page trimDirectiveWhitespaces="true" %>
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
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Redis控制台</title>


    <!-- Bootstrap Core CSS -->
    <link href="${ctx}/css/bootstrap.min.css" rel="stylesheet">

    <!-- MetisMenu CSS -->
    <link href="${ctx}css/metisMenu.min.css" rel="stylesheet">

    <!-- DataTables CSS -->
    <link href="${ctx}css/dataTables.bootstrap.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link href="${ctx}css/sb-admin-2.css" rel="stylesheet">

    <!-- Custom Fonts -->
    <link href="${ctx}css/font-awesome.min.css" rel="stylesheet"
          type="text/css">
    <link href="${ctx}css/boot-crm.css" rel="stylesheet"
          type="text/css">
    <style type="text/css">
        .btn-file { /*  上传按钮*/
            position: relative;
            overflow: hidden;
        }

        .btn-file input[type=file] {
            position: absolute;
            top: 0;
            right: 0;
            min-width: 100%;
            min-height: 100%;
            font-size: 100px;
            text-align: right;
            filter: alpha(opacity=0);
            opacity: 0;
            outline: none;
            background: white;
            cursor: inherit;
            display: block;
        }
    </style>

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
    <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
    <script type="text/javascript">
        var ctx="${ctx}";
    </script>
</head>

<body>
<div class="navbar navbar-default navbar-fixed-top" role="navigation">
    <div class="container">
        <div class="navbar-header">
            <a class="navbar-brand" href="${ctx}/index">Redis控制台</a>
        </div>
        <div class="navbar-collapse collapse">
            <ul class="nav navbar-nav">
                <li class="active"><a href="/index">Data</a></li>
                <li><a href="">Info</a></li>
                <li><a href="">Config</a></li>
                <li class="dropdown ">
                    <a href="">Offset</a>
                </li>
            </ul>
        </div><!--/.nav-collapse -->
    </div>
</div>

<div style="height: 60px"></div>
<div class="navbar-default sidebar" role="navigation" style="width: 9%;height:100%;margin-top: 0px;">
    <div class="sidebar-nav navbar-collapse">
        <ul class="nav" id="side-menu">
            <li><a href="${ctx}/index" ><i
                    class="fa fa-fw"></i>全部</a></li>
            <li><a href="javascript:void(0);" id="string"><i
                    class="fa fa-fw"></i> string</a></li>
            <li><a href="${ctx}/list"><i
                    class="fa  fa-fw"></i> list</a></li>
            <li><a href="${ctx}/set"><i
                    class="fa  fa-fw"></i> set</a></li>
            <li><a href="${ctx}/zSet"><i
                    class="fa  fa-fw"></i> zSet</a></li>
            <li><a href="${ctx}/hash"><i
                    class="fa  fa-fw"></i> hash</a></li>
        </ul>
    </div>
    <!-- /.sidebar-collapse -->
</div>
<!--模态框-->
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

<div class="modal fade"  tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
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
                <button type="button" class="btn btn-danger" >确认</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<div class="container theme-showcase" style="width: 82%;border: 1px solid #ddd;">
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
            <button type="button" class="btn btn-danger" data-toggle="modal" data-target="#myModal" >全部删除</button>
            <form class="form-inline" action="${ctx}"
                  method="post" style="display: inline">
                <div class="form-group">
                    <label for="customerName">key</label>
                    <input type="text" class="form-control" id="customerName" value="${custName }" name="custName">
                </div>
                <div class="form-group">
                    <label for="customerFrom">类型</label>
                    <select class="form-control" id="customerFrom" placeholder="客户来源" name="custSource">
                        <option value="">--请选择--</option>
                        <c:forEach items="${fromType}" var="item">
                            <option value="${item.dict_id}"<c:if
                                    test="${item.dict_id == custSource}"> selected</c:if>>${item.dict_item_name }</option>
                        </c:forEach>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary">查询</button>
            </form>
        </div>
    </div>
    <div class="row">
        <div class="col-lg-12">
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
                                       onclick="deleteCustomer(${row.cust_id})">删除</a>
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


<!-- jQuery -->
<script type="text/javascript" src="${ctx}js/jquery.min.js"></script>

<!-- Bootstrap Core JavaScript -->
<script type="text/javascript" src="${ctx}js/bootstrap.min.js"></script>

<!-- Metis Menu Plugin JavaScript -->
<script type="text/javascript" src="${ctx}js/metisMenu.min.js"></script>

<!-- DataTables JavaScript -->
<script type="text/javascript" src="${ctx}js/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="${ctx}js/dataTables.bootstrap.min.js"></script>

<!-- Custom Theme JavaScript -->
<script type="text/javascript" src="${ctx}js/sb-admin-2.js"></script>

<script type="text/javascript" src="${ctx}/js/jquery.form.min.js"></script>

<script type="text/javascript" src="${ctx}/js/redis/index.js"></script>

</body>

</html>
