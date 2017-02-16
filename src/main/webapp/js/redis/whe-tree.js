/**
 * Created by trustme on 2017/2/16.
 *
 */
;
(function ($, window, document, undefined) {
    var tree = String();
    var nodeId = 0;
    $.fn.initTree = function (data) {
        if (data != null && data.length >= 0) {
            var floor = Number(0);
            tree = '<ul class="list-group">';
            appendTree(data, floor);
            tree += '</ul>';
            $(this).addClass("treeview");
            $(this).html(tree);

            $(this).on('click', '.node-tree', function (event, data) {
                $(this).siblings(".list-group-item").removeClass("node-selected");
                if (!$(this).hasClass("node-selected")) {
                    $(this).addClass("node-selected");
                }
            });

            $(this).on('dblclick', '.node-tree', function () {

                var nodeId = $(this).attr("data-nodeid");
                var siblings = $(this).siblings(".list-group-item[parent-id='" + nodeId + "'] ");
                if ($(this).find(".expand-icon").hasClass("glyphicon-chevron-right")) {

                    $(this).find(".expand-icon").removeClass("glyphicon-chevron-right").addClass("glyphicon-chevron-down");
                } else {
                    $(this).siblings(".list-group-item").find(".expand-icon").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-right")
                }
                if (siblings.hasClass("show-node")) {
                    siblings.removeClass("show-node").addClass("hide-node");
                    siblings.each(function () {
                        $(this).siblings(".list-group-item[parent-id='" + $(this).attr("data-nodeid") + "'] ").removeClass("show-node").addClass("hide-node");
                    })
                } else {
                    siblings.removeClass("hide-node").addClass("show-node");
                }
            })
        }

    };
    var parentId = 0;

    function appendTree(data, floor) {
        if (!(data == null || data.length == 0)) {
            for (var i = 0; i < data.length; i++) {
                var obj = data[i];
                if (obj.page != null) {
                    tree += '<li class="list-group-item ';
                    if (obj.expanded != null && obj.expanded) {
                        tree += ' show-node';
                    } else {
                        tree += ' hide-node';
                    }
                    tree += ' " parent-id="' + parentId + '" data-nodeid="' + (++nodeId) + '" >';
                    tree += obj.page;
                    tree += '</li>';
                    continue;
                }
                tree += '<li class="list-group-item node-tree ';
                if (obj.expanded != null && obj.expanded) {
                    tree += ' show-node';
                } else {
                    tree += ' hide-node';
                }
                tree += ' " parent-id="' + parentId + '" data-nodeid="' + (++nodeId) + '" >';
                for (var j = 0; j < floor; j++) {
                    tree += '<span class="indent"></span>';
                }
                if (obj.nodes != null && obj.nodes.length > 0) {
                    tree += '<span class="icon expand-icon glyphicon  glyphicon-chevron-right"></span>';
                } else {
                    tree += '<span class="icon  glyphicon "></span>';
                }
                if (obj.icon != null) {
                    tree += '<img class="" src="' + obj.icon + '"/>';
                } else {
                    tree += '<span class="icon node-icon"></span>';
                }

                tree += '<span class="text">' + obj.text + '</span>';
                if (obj.tags != null) {
                    tree += '<span class="badge">' + obj.tags + '</span>';
                }

                tree += '</li>';
                if (obj.nodes != null) {
                    floor += 1;
                    parentId = nodeId;
                    arguments.callee(obj.nodes, floor);
                    floor--;
                    parentId = i - i + 1;
                } else {
                }
            }
        } else {
            return null;
        }
        $.fn.addNode = function (id, data) {
            var nodeData = String();
            for (var i = 0; i < data.length; i++) {
                nodeData += '<li class="list-group-item node-tree  show-node " parent-id="' + id + '" data-nodeid="' + (++nodeId) + '"><span class="indent"></span><span class="indent"></span><span class="icon  glyphicon  "></span><span class="text">' + data[i] + '</span></li>';
            }
            $(".list-group").find(".list-group-item[data-nodeid='" + id + "']").after(nodeData);
        }
    }
})(jQuery, window, document);