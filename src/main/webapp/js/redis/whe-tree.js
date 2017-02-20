/**
 * Created by trustme on 2017/2/16.
 *
 */
;
(function ($, window, document, undefined) {
    var tree = String();
    var nodeId = Number(0);
    $.fn.initTree = function (data) {
        if (data != null && data.length >= 0) {
            tree = '<ul class="list-group">';
            appendTree(data);
            tree += '</ul>';
            $(this).addClass("treeView");
            $(this).html(tree);

            $(this).on('click', '.node-div', function (event, data) {
                $(".treeView").find(".node-div").removeClass("node-selected");
                if (!$(this).hasClass("node-selected")) {
                    $(this).addClass("node-selected");
                }
            });

            $(this).on('dblclick', '.node-div', function () {
                if ($(this).find(".expand-icon").hasClass("glyphicon-chevron-right")) {
                    $(this).find(".expand-icon").removeClass("glyphicon-chevron-right").addClass("glyphicon-chevron-down");
                    $(this).siblings().removeClass("hide-node").addClass("show-node");
                } else {
                    $(this).find(".expand-icon").removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-right")
                    $(this).siblings().removeClass("show-node").addClass("hide-node");
                }
            });
            $(".expand-icon").on('click', function () {
                if ($(this).hasClass("glyphicon-chevron-right")) {
                    $(this).removeClass("glyphicon-chevron-right").addClass("glyphicon-chevron-down");
                    $(this).parent(".node-div").siblings().removeClass("hide-node").addClass("show-node");
                } else {
                    $(this).removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-right")
                    $(this).parent(".node-div").siblings().removeClass("show-node").addClass("hide-node");
                }

            })
        }

    };

    function appendTree(data) {
        if (!(data == null || data.length == 0)) {
            for (var i = 0; i < data.length; i++) {
                var obj = data[i];
                if (obj.page != null) {
                    tree += '<li >' + obj.page + '</li>';
                    continue;
                }
                tree += '<li class="node-tree" node-id="' + (nodeId++) + '" ><div class="node-div">';
                if (obj.nodes != null && obj.nodes.length > 0) {
                    tree += '<span class="icon expand-icon glyphicon  ';
                    if (obj.expanded != null && obj.expanded) {
                        tree += 'glyphicon-chevron-down"></span>';
                    } else {
                        tree += 'glyphicon-chevron-right"></span>';
                    }
                } else {
                    tree += '<span class="icon  glyphicon "></span>';
                }
                if (obj.icon != null) {
                    tree += '<img class="" src="' + obj.icon + '"/>';
                } else {
                    tree += '<span class="icon node-icon"></span>';
                }

                tree += '<span class="text" ';
                if (obj.type != null) {
                    tree += ' type="' + obj.type + '"';
                }
                tree += '>' + obj.text + '</span>';
                if (obj.tags != null) {
                    tree += '<span class="badge">' + obj.tags + '</span>';
                }
                tree += '</div>';
                if (obj.nodes != null) {
                    tree += '<ul class="child_ul';
                    if (obj.expanded != null && obj.expanded) {
                        tree += ' show-node">';
                    } else {
                        tree += ' hide-node">';
                    }
                    arguments.callee(obj.nodes);
                    tree += '</ul></li>';
                }
            }
        } else {
            return null;
        }
        $.fn.addNode = function (data) {
            tree = '';
            appendTree(data);
            $(this).closest(".child_ul").html(tree);
        }
    }
})(jQuery, window, document);