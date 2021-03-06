jQuery(function($) {
    var goodsId = $('#goodsId').val();

    /**
     *点击加入购物车按钮
     */
    $("#link_add_cart").click(function(e) {
        e.preventDefault();
        var element = $("#number").val();
        var boughtNumber = Number($("#boughtNumber").val());
        var addCartNumber = Number($("#addCartNumber").val()) + element * 1;
        var limitNumber = Number($("#limit_" + goodsId).val());
        if (limitNumber > 0 && limitNumber - boughtNumber > 0 && addCartNumber > (limitNumber - boughtNumber)) {
            $(".error").html("<strong style='display: block;'>已经超过限购数量，不能继续加入购物车！</strong>").css('color', '#F55');
            return false;
        }

        if (limitNumber > 0 && element > (limitNumber - boughtNumber)) {
            element.val(limitNumber - boughtNumber);
            return false;
        }

        $.post(
            "/carts",
            {'goodsId':goodsId, 'increment':$("#number").val()},
            function (data) {
                $("#addCartNumber").val(addCartNumber);
                $('#add_cart_result').show();
                //5秒后自动消失
                setTimeout("$('#add_cart_result').css('display','none')", 5000);
                //显示最新的购物车信息
                $("#result-count").text(data.count);
                $("#result-amount").text(data.amount);

                //修改顶部购物车商品数量
                $("#cart-count").html(data.count);
                $("#reload").val("true");
                $("#cart-js").html(data.count);
                $("#order_confirm").hide();
            }
        );
    });

    $("#link_buy_now").click(function () {
        var limitNumber = '${goods.limitNumber}';
        var number = Number($("#number").val());
        if (limitNumber > 0 && number > limitNumber) {
            $(".error").html("<strong>只能购买" + limitNumber + "个</strong>").css('color', '#F55');
            return false;
        }

        $("#order_create_form").submit();
        return false;
    });

    $("#J_closeTips").click(function (ev) {
        ev.preventDefault();
        $('#add_cart_result').hide();
    });

    //点击增加按钮
    $("#increase-btn").click(function () {
        reorder($(this).attr("name"), 1);
        return false;
    });
    //点击减少按钮
    $("#decrease-btn").click(function () {
        reorder($(this).attr("name"), -1);
        return false;
    });

    function reorder(goods_id, increment) {
        var element = $("#number");
        var last_num_ele = $("#last_num_" + goods_id);
        var stock = Number($("#stock_" + goods_id).val());
        var limitNumber = Number($("#limit_" + goods_id).val());
        var last_num = Number(last_num_ele.val())
        var new_num = last_num + increment;
        var boughtNumber = Number($("#boughtNumber").val());
        if (new_num <= 0) {
            element.val(last_num);
            return;
        }
        if (new_num > 999) {
            new_num = 999;
            increment = 999 - last_num;
        }
        if (new_num > stock) {
            new_num = stock;
            increment = stock - last_num;
            $("#stock_hit").css("display", "inline-block");
            return;
        } else {
            $("#stock_hit").css("display", "none");
        }

        if (limitNumber > 0 && new_num > (limitNumber - boughtNumber)) {
            element.val(limitNumber - boughtNumber);
            increment = limitNumber - last_num;
            return false;
        }

        if (increment == 0) {
            element.val(last_num);
            return;
        }
        element.val(new_num);
        last_num_ele.val(new_num);
    }
    

    // 主图切换
    $('#gallery').slides({
        play:5000,
        pause:2500,
        slideSpeed:500,
        hoverPause:true
    }).hover(
        function() {
            $('#gallery .btn').show();
        },
        function() {
            $('#gallery .btn').hide();
        }
    );
    // 右边广告切换
    $('#switch').slides({
        play:4000,
        pause:2000,
        slideSpeed:300,
        hoverPause:true
    });
    // 分享
    $('#qq').click(function(e) {
        e.preventDefault();
        $('#share-im').slideToggle(100);
    });
    $('#share-url').click(function(){
        $(this).select();
    });


    /**  
     * 地图相关
     */
    // blockUI 默认样式
    $.blockUI.defaults.css = {
        padding:	0,
        margin:		0,
        top:        '20%',
        left:		'35%',
        textAlign:  'left',
        color:		'#000',
        border:     '3px solid #cecece',
        backgroundColor:'#fff',
        cursor:     'default'
    };
    $.blockUI.defaults.overlayCSS = {
        backgroundColor:	'#000',
        opacity:			0.6,
        cursor:				'default'
    };

    var minGmap,
        bigGmap,
        mapOpts = {
            latlngStr: '',
            latlng: {},
            title: ''
        },
        currPage = 1,
        pageSize = 5,
        totalPage,
        outletUrl,
        consultUrl,
        outletPaging = $('#outlet-page'),
        geocoder = new google.maps.Geocoder();


    if (location.host == "127.0.0.1") {
        outletUrl = '/zome/home/template/outletList.php';
        consultUrl = '/zome/home/template/consult.php';
        addConsultUrl = '/zome/home/template/addConsult.php';
    } else {
        outletUrl = '/goods/'+ goodsId +'/shops'; 
        consultUrl = '/goods/'+ goodsId +'/questions'; 
        addConsultUrl = '/user-question';
    }

    function initMap() {
        $.getJSON(outletUrl, 'currPage='+ currPage +'&pageSize='+ pageSize, function(data){
            if (data.status == 0) {
                callback(data.outletList);
                totalPage = Math.ceil(data.totalOutlet/5);
                $('#outlet-total-num').text(data.totalOutlet);
            } else {
                $('#outlet').hide();
            }

            if (totalPage > 1) {
                outletPaging.html('<a class="next-page" href="#">下一页</a>');
            }
        });

    }
    initMap();

    function createMap(id, zoom, latlng, title) {
        var options = {
            zoom: zoom,
            center: latlng,
            zoomControl: true,
            panControl:  false,
            scaleControl: false,
            overviewMapControl: false,
            streetViewControl: false,
            mapTypeControl: false,
            mapTypeId: google.maps.MapTypeId.ROADMAP
        },

        map = new google.maps.Map(document.getElementById(id), options),

        marker = new google.maps.Marker({
            position: latlng,
            map: map,
            animation: google.maps.Animation.DROP,
            title: title
        });
        return {
            map: map,
            marker: marker
        };
    }
    function chgMinGmap(str, arr, tit) {
        mapOpts.latlngStr = str;
        mapOpts.latlng = new google.maps.LatLng(arr[0], arr[1]);
        mapOpts.title = tit;

        if (minGmap === undefined) {
            minGmap = createMap('min_gmap', 13, mapOpts.latlng, mapOpts.title);
        }

        minGmap.map.setCenter(mapOpts.latlng);
        minGmap.marker.setMap(null);
        minGmap.marker = new google.maps.Marker({
            position: mapOpts.latlng,
            map: minGmap.map,
            animation: google.maps.Animation.DROP,
            title: mapOpts.title
        });
    }
    // 切换店面地址
    $('.outlet-name').live('click', function(){
        var $this = $(this);
        $('.outlet-name').removeClass('outlet-show');
        $this.addClass('outlet-show');
        $('.outlet-attr:visible').slideUp(100);
        $this.siblings().slideToggle(100);

        var str = $this.attr('data-latlng');
        if (str != '') {
            arr = str.split(',');
            chgMinGmap(str, arr, $this.text());
        } else {
            // 根据当前地址查找经纬度
            geocoder.geocode({'address': $this.attr('data-addr')}, function (results, status) {
                if (status == google.maps.GeocoderStatus.OK) {
                    var location = results[0].geometry.location;
                    str = location.lat() +','+ location.lng();
                    arr = str.split(',');
                } else {
                    str = '31.001197278248362,122.25685396265624'; // google 找不到当前地址时，默认显示到太平洋 
                    arr = [31.001197278248362, 122.25685396265624];
                }
                $this.attr('data-latlng', str);
                chgMinGmap(str, arr, $this.text());
            });
        }

    });
    function doHtml(data) {
        var html = '';
        for (i in data) {
            html += '<li>'
                +'<h5 class="outlet-name" data-addr="'+ data[i].addr +'" data-latlng="'+ data[i].latlng +'">'+ data[i].name +'</h5>'
                +'<div class="outlet-attr">'
                +'    <p>'+ data[i].addr +'</p>'
                +'    <p><span>'+ data[i].tel +'</span> <a class="view-map" href="#">查看地图»</a> <a class="search-path" href="#">公交/驾车»</a></p>'
                +'</div>'
                +'</li>';
        }
        $('.outlet-list ul').html(html);
        $('.outlet-attr:first').show();
        $('.outlet-name:first').addClass('outlet-show');
    }

    function callback(data) {
        var str, arr;
        if (data[0]['latlng'] != '') {
            str = data[0]['latlng'];
            arr = str.split(',');
            doHtml(data);
            chgMinGmap(str, arr, data[0]['name']);
        } else {
            // 根据当前地址查找经纬度
            geocoder.geocode({'address': data[0]['addr']}, function (results, status) {
                if (status == google.maps.GeocoderStatus.OK) {
                    var location = results[0].geometry.location;
                    str = location.lat() +','+ location.lng();
                    arr = str.split(',');
                } else {
                    str = '31.001197278248362,122.25685396265624'; // google 找不到当前地址时，默认显示到太平洋 
                    arr = [31.001197278248362, 122.25685396265624];
                }
                data[0]['latlng'] = str;
                doHtml(data);
                chgMinGmap(str, arr, data[0]['name']);
            });
        }
    }
    // 查看地图
    $('.view-map').live('click', function(e){
        e.preventDefault();
        $.blockUI({
            css: {
                width: 826,
                top: $(window).height()/2 - 284 +'px',
                left: $(window).width()/2 - 413 +'px'
            },
			focusInput: false,
            message: $('<div id="map_box">'
                    +    '<a class="close" href="javascript:void(0)" hidefocus="true"></a>'
                    +    '<h3>' + mapOpts.title + '</h3>'
                    +    '<div id="big_gmap" style="width:800px;height:500px;"></div>'
                    +    '<p>提醒：地图标注位置仅供参考，具体情况以实际道路标识信息为准</p>'
                    + '</div>')
        });
        $('.blockOverlay, .close').attr('title','单击关闭').click($.unblockUI); 

        bigGmap = createMap('big_gmap', 15, mapOpts.latlng, mapOpts.title);
        bigGmap.map.setCenter(mapOpts.latlng);
        bigGmap.marker.setMap(null);
        bigGmap.marker = new google.maps.Marker({
            position: mapOpts.latlng,
            map: bigGmap.map,
            animation: google.maps.Animation.DROP,
            title: mapOpts.title
        });
    });
    // 公交/驾车
    $('.search-path').live('click', function(e){
        e.preventDefault();
        $.blockUI({
            css: {
                width: 320,
                top: $(window).height()/2 - 94 +'px',
                left: $(window).width()/2 - 160 +'px'
            },
			focusInput: false,
            message: $('<div id="map_search">'
                    +   '<a class="close" href="javascript:void(0)" hidefocus="true"></a>'
                    +   '<h3>查询路线</h3>'
                    +   '<form action="http://ditu.google.cn/maps" method="get" target="_blank">'
                    +      '<ul>'
                    +          '<li><span class="text">目的地</span> <span id="daddr-txt">'+ mapOpts.title +'</span><input type="hidden" id="daddr-val" name="daddr" value="'+ mapOpts.latlngStr +'"></li>'
                    +          '<li><span class="text">出行方式</span> <input type="radio" name="dirflg" checked value="r">公交 <input type="radio" name="dirflg" value="d">驾车</li>'
                    +          '<li><span class="text">出发地</span> <input type="text" name="saddr" class="saddr"></li>'
                    +          '<li><button type="submit" class="btn">查询</button></li>'
                    +      '</ul>'
                    +   '</form>'
                    +'</div>')
        });
        $('.blockOverlay, .close').attr('title','单击关闭').click($.unblockUI); 
    });
    // 门店分页
    $('#outlet-page').delegate('a','click', function(e){
        e.preventDefault();
        if ($(this).hasClass('next-page')) {
            currPage++;
            $.getJSON(outletUrl, 'currPage='+ currPage +'&pageSize='+ pageSize, function(data){
                callback(data.outletList);

                if ( totalPage <=2 ) {
                    outletPaging.html('<a class="prev-page" href="#">上一页</a>');
                } else if (currPage == totalPage) {
                    outletPaging.html('<a class="prev-page" href="#">上一页</a>');
                } else {
                    outletPaging.html('<a class="prev-page" href="#">上一页</a> | <a class="next-page" href="#">下一页</a>');
                }
            });
        }
        if ($(this).hasClass('prev-page')) {
            currPage--;
            $.getJSON(outletUrl, 'currPage='+ currPage +'&pageSize='+ pageSize, function(data){
                callback(data.outletList);

                if (currPage == 1) {
                    outletPaging.html('<a class="next-page" href="#">下一页</a>');
                } else {
                    outletPaging.html('<a class="prev-page" href="#">上一页</a> | <a class="next-page" href="#">下一页</a>');
                }
            });
        }
    });

    /**
     * 咨询
     */
    var pageObj = {
        currPage: 1,
        pageSize: 5
    };
    function paging(page, count) {
        var strHtml = "";
          
        if (page > 1) {
            strHtml += '<a class="prev" href="javascript:void(0)" data-page="'+ (page-1) +'"><i></i>上一页</a>';
        } else {
            strHtml += '<span class="prev"><i></i>上一页</span>';
        }

        if (count <= 10) {
            for (var i = 1; i <= count; i++) {
                if (page == i) {
                    strHtml += '<span class="curr">'+ i +'</span>';
                } else {
                    strHtml += '<a href="javascript:void(0)" data-page="'+ i +'">'+ i +'</a>';
                }
            }
        } else {
            if (page < 4) {
                for (var i = 1; i < page; i++) {
                    strHtml += '<a href="javascript:void(0)" data-page="'+ i +'">'+ i +'</a>';
                }
                strHtml += '<span class="curr">'+ page +'</span>';
                strHtml += '<a href="javascript:void(0)" data-page="'+ (Number(page)+1) +'">'+ (Number(page)+1) +'</a>';
                strHtml += '<a href="javascript:void(0)" data-page="'+ (Number(page)+2) +'">'+ (Number(page)+2) +'</a>';
                strHtml += '<span class="omit">...</span>';
                strHtml += '<a href="javascript:void(0)" data-page="'+ count +'">'+ count +'</a>';
            }
            if (page >= 4 && (page <= count-3)) {
                strHtml += '<a href="javascript:void(0)" data-page="1">1</a>';
                strHtml += '<span class="omit">...</span>';
                strHtml += '<a href="javascript:void(0)" data-page="'+ (Number(page)-2) +'">'+ (Number(page)-2) +'</a>';
                strHtml += '<a href="javascript:void(0)" data-page="'+ (Number(page)-1) +'">'+ (Number(page)-1) +'</a>';
                strHtml += '<span class="curr">'+ page +'</span>';
                strHtml += '<a href="javascript:void(0)" data-page="'+ (Number(page)+1) +'">'+ (Number(page)+1) +'</a>';
                strHtml += '<a href="javascript:void(0)" data-page="'+ (Number(page)+2) +'">'+ (Number(page)+2) +'</a>';
                strHtml += '<span class="omit">...</span>';
                strHtml += '<a href="javascript:void(0)" data-page="'+ count +'">'+ count +'</a>';
            }
            if (page > count-3) {
                strHtml += '<a href="javascript:void(0)" data-page="1">1</a>';
                strHtml += '<span class="omit">...</span>';
                strHtml += '<a href="javascript:void(0)" data-page="'+ (Number(page)-2) +'">'+ (Number(page)-2) +'</a>';
                strHtml += '<a href="javascript:void(0)" data-page="'+ (Number(page)-1) +'">'+ (Number(page)-1) +'</a>';
                strHtml += '<span class="curr">'+ page +'</span>';
                for (var i = page+1; i<=count; i++) {
                    strHtml += '<a href="javascript:void(0)" data-page="'+ i +'">'+ i +'</a>';
                }
            }
        }

        if (page < count) {
            strHtml += '<a class="next" href="javascript:void(0)" data-page="'+ (Number(page)+1) +'"><i></i>下一页</a>';
        } else {
            strHtml += '<span class="next"><i></i>下一页</span>';
        }
        
        return strHtml;
    }

    function tmpl(question, qdate, answer, adate) {
        return '<li><dl class="question"><dt>咨询内容：</dt><dd>'+ question +'<span class="date">'+ qdate +'</span></dd></dl>'
               +'<dl class="answer"><dt>客服回复：</dt><dd>'+ answer +'<span class="date">'+ adate +'</span></dd></dl></li>';
    }

    function async() {
        $.getJSON(consultUrl, 'currPage='+ pageObj.currPage +'&pageSize='+ pageObj.pageSize, function(data){
            var list = data.list,
                html = '';
            pageObj.pageCount = Math.ceil(data.total / pageObj.pageSize);

            if (list.length > 0) {
                for (i in list) {
                    html += tmpl(list[i].question, list[i].qdate, list[i].answer, list[i].adate);
                }
                $('.consult-list').html(html);
                $('#consult .pagination').html(paging(pageObj.currPage, pageObj.pageCount));
            }
        });
    }
    $('#consult .pagination').delegate('a', 'click', function(e){
        e.preventDefault();
        pageObj.currPage = $(this).attr('data-page');
        async();
    });
    async();

    Date.prototype.format = function(fmt) {
        var o = {
            "M+": this.getMonth() + 1, //月份
            "d+": this.getDate(), //日
            "h+": this.getHours(), //小时
            "m+": this.getMinutes(), //分
            "s+": this.getSeconds(), //秒
            "q+": Math.floor((this.getMonth() + 3) / 3) //季度
        };
        if (/(y+)/.test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
        }
        for (var k in o) {
            if (new RegExp("(" + k + ")").test(fmt)) {
                fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
            }
        }
        return fmt;
    }

    $('#submit').click(function(e){
        e.preventDefault();
        var qVal = $('#question').val(),
            mobi = $('#mobile'),
            mVal = mobi.length == 1 ? mobi.val() : '',
            date = new Date().format('yyyy-MM-dd hh:mm:ss'),
            error = $('#consult-form .error'),
            timer,
            errTips = function(txt) {
                error.html(txt).show();
                clearTimeout(timer);
                timer = setTimeout(function(){
                    error.hide();
                }, 3000);
            };

        if (qVal == '') {
            errTips('请输入咨询的问题');
            return;
        }
        if (mVal != '' && !(/^(1\d{10})$/i).test(mVal)) {
            errTips('请输入正确的手机号');
            return;
        }

        $.getJSON(addConsultUrl, {'content': qVal, 'mobile': mVal, 'goodsId':goodsId}, function(data){
            if (data.error == '') {
                $('.consult-list').prepend(tmpl(qVal, date, '请耐心等待回复，谢谢', ''));
                errTips('您的咨询发表成功，请耐心等待回复，谢谢');
            } else {
                errTips(data.error);
            }
        });
    });

    (new GoTop()).init({
        pageWidth           :960,
        nodeId              :'go-top',
        nodeWidth           :24,
        distanceToBottom    :100,
        distanceToPage      :10,
        hideRegionHeight    :130,
        text                :''
    });
});
