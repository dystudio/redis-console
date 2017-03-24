<#assign ctx=request.contextPath />
<!DOCTYPE html>
<html lang="en">
<head>
    <!--浏览器兼容，兼容IE-->
    <meta charset="utf-8" http-equiv="X-UA-Compatible" content="IE=edge">
    <!-- 上述3个meta标签*必须*放在最前面，任何其他内容都*必须*跟随其后！ -->
    <title>Redis控制台</title>
    <!-- 1.导入css -->
    <link href="${ctx!}/static/css/bootstrap.css" rel="stylesheet">
    <link href="${ctx!}/static/css/index.css" rel="stylesheet">

    <!--兼容ie6、7、8的浏览器，他们都不支持html5-->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
    <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->

    <script type="text/javascript">
        var ctx = "${ctx!}";
    </script>

</head>

<body>
<div class="navbar navbar-default" role="navigation">
    <div style="margin-left: 100px;">
        <div class="navbar-header">
            <a class="navbar-brand redisAll" href="javascript:void(0);">Redis控制台</a>
        </div>
        <div class="navbar-collapse collapse">
            <ul class="nav navbar-nav">
                <li class="active dropdown redisAll">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                    <#if server??>
                        <#if server=="/sentinel">Sentinel
                        <#elseif server=="/cluster">Cluster
                        <#else>Standalone
                        </#if>
                    <#else>Standalone
                    </#if>
                        <span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu">
                        <li><a href="${ctx!}/standalone/index">Standalone</a></li>
                        <li><a href="${ctx!}/sentinel/index">Sentinel</a></li>
                        <li><a href="${ctx!}/cluster/index">Cluster</a></li>
                    </ul>
                </li>
                <li><a href="javascript:void(0);" class="noExploit">Info</a></li>
                <li><a href="javascript:void(0);" class="noExploit">Config</a></li>
            </ul>
        </div>
    </div>
</div>
<div class="left" style="">
    <div class="leftMenu">
        <!--添加按钮-->
        <button type="button" class="btn btn-success" data-toggle="modal" data-target="#addServerModal">
            <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>添加
        </button>
        <!--修改按钮-->
        <button id="btnEdit" type="button" class="btn btn-warning">
            <span class="glyphicon glyphicon-edit" aria-hidden="true"></span>修改
        </button>
        <!--删除按钮---无弹出框-->
        <button id="btnDel" type="button" class="btn btn-danger" data-toggle="modal" data-target="#DeleteForm"
                onclick="">
            <span class="glyphicon glyphicon-minus" aria-hidden="true"></span>删除
        </button>
    </div>

    <div id="tree"></div>
</div>
<div class="container-div theme-showcase">
    <!-- /.row -->
    <div class="panel">
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
            <form class="form-inline" action="${ctx!}${server!}/index"
                  method="post" style="display: inline">
                <div class="form-group">
                    <label>key</label>
                    <input type="text" class="form-control" id="match" value="${match!}" name="match">
                </div>
                <button type="submit" class="btn btn-primary">查询</button>
            </form>
        </div>
    </div>
    <div id="redisContent">
    </div>
</div>

<#include "modal.ftl">

</body>
<!-- 2 jQuery类库 -->
<script src="${ctx!}/static/js/jquery.min.js"></script>
<script src="${ctx!}/static/js/jquery.form.min.js"></script>

<!-- 3 bootstrap 类库 -->
<script src="${ctx!}/static/js/bootstrap.min.js"></script>
<script src="${ctx!}/static/js/redis/index.js"></script>
<script src="${ctx!}/static/js/redis/whe-tree.js"></script>
<script type="text/javascript">
    var defaultData = ${tree!};
    var server = '${server!}';
    var match = '${match!}';
    var redisView = 0;
    var serialize = "";
    $("#tree").initTree(defaultData);
    function add() {
        var serverForm = $("#serverForm");
        var checkFlag = true;
        if ($("#serverName").val().trim() == "") {
            $("#serverName").closest(".form-group").addClass("has-error");
            checkFlag = false;
        } else {
            $("#serverName").closest(".form-group").removeClass("has-error");
        }
        if ($("#serverHost").val().trim() == "") {
            $("#serverHost").closest(".form-group").addClass("has-error");
            checkFlag = false;
        } else {
            $("#serverHost").closest(".form-group").removeClass("has-error");
        }
        if ($("#serverPort").val().trim() == "") {
            $("#serverPort").closest(".form-group").addClass("has-error");
            checkFlag = false;
        } else {
            $("#serverPort").closest(".form-group").removeClass("has-error");
        }
        if (!checkFlag) {
            return;
        }
        var options = {
            url: ctx + "/add",
            type: "post",
            dataType: "text",
            success: function (data) {
                showModel(data);
            }
        };
        $("#addServerModal").modal('hide');
        $("#serverForm").ajaxSubmit(options);
    }
    //添加
    $("#addRedis").on('click', function () {
        if (key != null && redisType != string) {
            $("#redis_key").val(key);
        } else {
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