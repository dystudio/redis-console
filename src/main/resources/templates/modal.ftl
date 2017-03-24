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
</div>
<!-- /.modal -->

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
</div>
<!-- /.modal -->
<div class="modal fade" id="addServerModal" tabindex="-1" role="dialog"
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
                <form class="form-horizontal" id="serverForm" action="">
                    <div class="form-group">
                        <label for="redis_type" style="float:left;padding:10px 15px 0 17px;">Redis类型</label>
                        <div class="col-sm-10">
                            <select class="form-control"  name="serverType">
                                <option value="standalone" selected>Standalone</option>
                                <option value="sentinel">Sentinel</option>
                                <option value="cluster">Cluster</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="name" class="col-sm-2 control-label">Name</label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" name="name" id="serverName">
                        </div>
                    </div>
                    <div class="form-group"  >
                        <label for="redis_field" class="col-sm-2 control-label">Host</label>
                        <div class="col-sm-10">
                            <input type="text" name="host" class="form-control" id="serverHost">
                        </div>
                    </div>
                    <div class="form-group"  >
                        <label for="redis_field" class="col-sm-2 control-label">Port</label>
                        <div class="col-sm-10">
                            <input type="number" name="port" class="form-control" value="6379" id="serverPort">
                        </div>
                    </div>
                    <div class="form-group"  >
                        <label for="redis_field" class="col-sm-2 control-label">password</label>
                        <div class="col-sm-10">
                            <input type="text" name="password" class="form-control">
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-success" onclick="add()">保存</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
            </div>
        </div>
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
                    <#if (server?? && server!="/cluster")>
                        <div class="form-group">
                            <label for="redis_data_size" style="float:left;padding:10px 15px 0 40px;">数据库</label>
                            <div class="col-sm-10">
                                <select class="form-control" id="redis_data_size" name="redis_data_size">
                                    <#if dataSize??>
                                        <#list 0..dataSize-1 as i>
                                            <option value="${i}">db-${i}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                    </#if>
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
                <button type="button" class="btn btn-success" id="redis_save">保存</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
            </div>
        </div>
    </div>
</div>