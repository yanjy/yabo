jQuery(function(g){var b=g("#goodsId").val();g("#link_add_cart").click(function(B){B.preventDefault();var A=g("#number").val();var z=Number(g("#boughtNumber").val());var x=Number(g("#addCartNumber").val())+A*1;var y=Number(g("#limit_"+b).val());if(y>0&&y-z>0&&x>(y-z)){g(".error").html("<strong style='display: block;'>已经超过限购数量，不能继续加入购物车！</strong>").css("color","#F55");return false}if(y>0&&A>(y-z)){A.val(y-z);return false}g.post("/carts",{goodsId:b,increment:g("#number").val()},function(C){g("#addCartNumber").val(x);g("#add_cart_result").show();setTimeout("$('#add_cart_result').css('display','none')",5000);g("#result-count").text(C.count);g("#result-amount").text(C.amount);g("#cart-count").html(C.count);g("#reload").val("true");g("#cart-js").html(C.count);g("#order_confirm").hide()})});g("#link_buy_now").click(function(){var x="${goods.limitNumber}";var y=Number(g("#number").val());if(x>0&&y>x){g(".error").html("<strong>只能购买"+x+"个</strong>").css("color","#F55");return false}g("#order_create_form").submit();return false});g("#J_closeTips").click(function(x){x.preventDefault();g("#add_cart_result").hide()});g("#increase-btn").click(function(){h(g(this).attr("name"),1);return false});g("#decrease-btn").click(function(){h(g(this).attr("name"),-1);return false});function h(B,D){var z=g("#number");var F=g("#last_num_"+B);var C=Number(g("#stock_"+B).val());var A=Number(g("#limit_"+B).val());var x=Number(F.val());var E=x+D;var y=Number(g("#boughtNumber").val());if(E<=0){z.val(x);return}if(E>999){E=999;D=999-x}if(E>C){E=C;D=C-x;g("#stock_hit").css("display","inline-block");return}else{g("#stock_hit").css("display","none")}if(A>0&&E>(A-y)){z.val(A-y);D=A-x;return false}if(D==0){z.val(x);return}z.val(E);F.val(E)}g("#gallery").slides({play:5000,pause:2500,slideSpeed:500,hoverPause:true}).hover(function(){g("#gallery .btn").show()},function(){g("#gallery .btn").hide()});g("#switch").slides({play:4000,pause:2000,slideSpeed:300,hoverPause:true});g("#qq").click(function(x){x.preventDefault();g("#share-im").slideToggle(100)});g("#share-url").click(function(){g(this).select()});g.blockUI.defaults.css={padding:0,margin:0,top:"20%",left:"35%",textAlign:"left",color:"#000",border:"3px solid #cecece",backgroundColor:"#fff",cursor:"default"};g.blockUI.defaults.overlayCSS={backgroundColor:"#000",opacity:0.6,cursor:"default"};var t,q,c={latlngStr:"",latlng:{},title:""},r=1,v=5,d,f,u,w=g("#outlet-page"),o=new google.maps.Geocoder();if(location.host=="127.0.0.1"){f="/zome/home/template/outletList.php";u="/zome/home/template/consult.php";addConsultUrl="/zome/home/template/addConsult.php"}else{f="/goods/"+b+"/shops";u="/goods/"+b+"/questions";addConsultUrl="/user-question"}function p(){g.getJSON(f,"currPage="+r+"&pageSize="+v,function(x){if(x.status==0){k(x.outletList);d=Math.ceil(x.totalOutlet/5);g("#outlet-total-num").text(x.totalOutlet)}else{g("#outlet").hide()}if(d>1){w.html('<a class="next-page" href="#">下一页</a>')}})}p();function s(D,z,C,B){var y={zoom:z,center:C,zoomControl:true,panControl:false,scaleControl:false,overviewMapControl:false,streetViewControl:false,mapTypeControl:false,mapTypeId:google.maps.MapTypeId.ROADMAP},A=new google.maps.Map(document.getElementById(D),y),x=new google.maps.Marker({position:C,map:A,animation:google.maps.Animation.DROP,title:B});return{map:A,marker:x}}function e(z,x,y){c.latlngStr=z;c.latlng=new google.maps.LatLng(x[0],x[1]);c.title=y;if(t===undefined){t=s("min_gmap",13,c.latlng,c.title)}t.map.setCenter(c.latlng);t.marker.setMap(null);t.marker=new google.maps.Marker({position:c.latlng,map:t.map,animation:google.maps.Animation.DROP,title:c.title})}g(".outlet-name").live("click",function(){var x=g(this);g(".outlet-name").removeClass("outlet-show");x.addClass("outlet-show");g(".outlet-attr:visible").slideUp(100);x.siblings().slideToggle(100);var y=x.attr("data-latlng");if(y!=""){arr=y.split(",");e(y,arr,x.text())}else{o.geocode({address:x.attr("data-addr")},function(B,A){if(A==google.maps.GeocoderStatus.OK){var z=B[0].geometry.location;y=z.lat()+","+z.lng();arr=y.split(",")}else{y="31.001197278248362,122.25685396265624";arr=[31.001197278248362,122.25685396265624]}x.attr("data-latlng",y);e(y,arr,x.text())})}});function n(y){var x="";for(i in y){x+='<li><h5 class="outlet-name" data-addr="'+y[i].addr+'" data-latlng="'+y[i].latlng+'">'+y[i].name+'</h5><div class="outlet-attr">    <p>'+y[i].addr+"</p>    <p><span>"+y[i].tel+'</span> <a class="view-map" href="#">查看地图»</a> <a class="search-path" href="#">公交/驾车»</a></p></div></li>'}g(".outlet-list ul").html(x);g(".outlet-attr:first").show();g(".outlet-name:first").addClass("outlet-show")}function k(y){var z,x;if(y[0]["latlng"]!=""){z=y[0]["latlng"];x=z.split(",");n(y);e(z,x,y[0]["name"])}else{o.geocode({address:y[0]["addr"]},function(C,B){if(B==google.maps.GeocoderStatus.OK){var A=C[0].geometry.location;z=A.lat()+","+A.lng();x=z.split(",")}else{z="31.001197278248362,122.25685396265624";x=[31.001197278248362,122.25685396265624]}y[0]["latlng"]=z;n(y);e(z,x,y[0]["name"])})}}g(".view-map").live("click",function(x){x.preventDefault();g.blockUI({css:{width:826,top:g(window).height()/2-284+"px",left:g(window).width()/2-413+"px"},focusInput:false,message:g('<div id="map_box"><a class="close" href="javascript:void(0)" hidefocus="true"></a><h3>'+c.title+'</h3><div id="big_gmap" style="width:800px;height:500px;"></div><p>提醒：地图标注位置仅供参考，具体情况以实际道路标识信息为准</p></div>')});g(".blockOverlay, .close").attr("title","单击关闭").click(g.unblockUI);q=s("big_gmap",15,c.latlng,c.title);q.map.setCenter(c.latlng);q.marker.setMap(null);q.marker=new google.maps.Marker({position:c.latlng,map:q.map,animation:google.maps.Animation.DROP,title:c.title})});g(".search-path").live("click",function(x){x.preventDefault();g.blockUI({css:{width:320,top:g(window).height()/2-94+"px",left:g(window).width()/2-160+"px"},focusInput:false,message:g('<div id="map_search"><a class="close" href="javascript:void(0)" hidefocus="true"></a><h3>查询路线</h3><form action="http://ditu.google.cn/maps" method="get" target="_blank"><ul><li><span class="text">目的地</span> <span id="daddr-txt">'+c.title+'</span><input type="hidden" id="daddr-val" name="daddr" value="'+c.latlngStr+'"></li><li><span class="text">出行方式</span> <input type="radio" name="dirflg" checked value="r">公交 <input type="radio" name="dirflg" value="d">驾车</li><li><span class="text">出发地</span> <input type="text" name="saddr" class="saddr"></li><li><button type="submit" class="btn">查询</button></li></ul></form></div>')});g(".blockOverlay, .close").attr("title","单击关闭").click(g.unblockUI)});g("#outlet-page").delegate("a","click",function(x){x.preventDefault();if(g(this).hasClass("next-page")){r++;g.getJSON(f,"currPage="+r+"&pageSize="+v,function(y){k(y.outletList);if(d<=2){w.html('<a class="prev-page" href="#">上一页</a>')}else{if(r==d){w.html('<a class="prev-page" href="#">上一页</a>')}else{w.html('<a class="prev-page" href="#">上一页</a> | <a class="next-page" href="#">下一页</a>')}}})}if(g(this).hasClass("prev-page")){r--;g.getJSON(f,"currPage="+r+"&pageSize="+v,function(y){k(y.outletList);if(r==1){w.html('<a class="next-page" href="#">下一页</a>')}else{w.html('<a class="prev-page" href="#">上一页</a> | <a class="next-page" href="#">下一页</a>')}})}});var a={currPage:1,pageSize:5};function l(A,z){var y="";if(A>1){y+='<a class="prev" href="javascript:void(0)" data-page="'+(A-1)+'"><i></i>上一页</a>'}else{y+='<span class="prev"><i></i>上一页</span>'}if(z<=10){for(var x=1;x<=z;x++){if(A==x){y+='<span class="curr">'+x+"</span>"}else{y+='<a href="javascript:void(0)" data-page="'+x+'">'+x+"</a>"}}}else{if(A<4){for(var x=1;x<A;x++){y+='<a href="javascript:void(0)" data-page="'+x+'">'+x+"</a>"}y+='<span class="curr">'+A+"</span>";y+='<a href="javascript:void(0)" data-page="'+(Number(A)+1)+'">'+(Number(A)+1)+"</a>";y+='<a href="javascript:void(0)" data-page="'+(Number(A)+2)+'">'+(Number(A)+2)+"</a>";y+='<span class="omit">...</span>';y+='<a href="javascript:void(0)" data-page="'+z+'">'+z+"</a>"}if(A>=4&&(A<=z-3)){y+='<a href="javascript:void(0)" data-page="1">1</a>';y+='<span class="omit">...</span>';y+='<a href="javascript:void(0)" data-page="'+(Number(A)-2)+'">'+(Number(A)-2)+"</a>";y+='<a href="javascript:void(0)" data-page="'+(Number(A)-1)+'">'+(Number(A)-1)+"</a>";y+='<span class="curr">'+A+"</span>";y+='<a href="javascript:void(0)" data-page="'+(Number(A)+1)+'">'+(Number(A)+1)+"</a>";y+='<a href="javascript:void(0)" data-page="'+(Number(A)+2)+'">'+(Number(A)+2)+"</a>";y+='<span class="omit">...</span>';y+='<a href="javascript:void(0)" data-page="'+z+'">'+z+"</a>"}if(A>z-3){y+='<a href="javascript:void(0)" data-page="1">1</a>';y+='<span class="omit">...</span>';y+='<a href="javascript:void(0)" data-page="'+(Number(A)-2)+'">'+(Number(A)-2)+"</a>";y+='<a href="javascript:void(0)" data-page="'+(Number(A)-1)+'">'+(Number(A)-1)+"</a>";y+='<span class="curr">'+A+"</span>";for(var x=A+1;x<=z;x++){y+='<a href="javascript:void(0)" data-page="'+x+'">'+x+"</a>"}}}if(A<z){y+='<a class="next" href="javascript:void(0)" data-page="'+(Number(A)+1)+'"><i></i>下一页</a>'}else{y+='<span class="next"><i></i>下一页</span>'}return y}function m(y,A,z,x){return'<li><dl class="question"><dt>咨询内容：</dt><dd>'+y+'<span class="date">'+A+'</span></dd></dl><dl class="answer"><dt>客服回复：</dt><dd>'+z+'<span class="date">'+x+"</span></dd></dl></li>"}function j(){g.getJSON(u,"currPage="+a.currPage+"&pageSize="+a.pageSize,function(z){var y=z.list,x="";a.pageCount=Math.ceil(z.total/a.pageSize);if(y.length>0){for(i in y){x+=m(y[i].question,y[i].qdate,y[i].answer,y[i].adate)}g(".consult-list").html(x);g("#consult .pagination").html(l(a.currPage,a.pageCount))}})}g("#consult .pagination").delegate("a","click",function(x){x.preventDefault();a.currPage=g(this).attr("data-page");j()});j();Date.prototype.format=function(x){var z={"M+":this.getMonth()+1,"d+":this.getDate(),"h+":this.getHours(),"m+":this.getMinutes(),"s+":this.getSeconds(),"q+":Math.floor((this.getMonth()+3)/3)};if(/(y+)/.test(x)){x=x.replace(RegExp.$1,(this.getFullYear()+"").substr(4-RegExp.$1.length))}for(var y in z){if(new RegExp("("+y+")").test(x)){x=x.replace(RegExp.$1,(RegExp.$1.length==1)?(z[y]):(("00"+z[y]).substr((""+z[y]).length)))}}return x};g("#submit").click(function(D){D.preventDefault();var A=g("#question").val(),x=g("#mobile"),C=x.length==1?x.val():"",z=new Date().format("yyyy-MM-dd hh:mm:ss"),y=g("#consult-form .error"),E,B=function(F){y.html(F).show();clearTimeout(E);E=setTimeout(function(){y.hide()},3000)};if(A==""){B("请输入咨询的问题");return}if(C!=""&&!(/^(1\d{10})$/i).test(C)){B("请输入正确的手机号");return}g.getJSON(addConsultUrl,{content:A,mobile:C,goodsId:b},function(F){if(F.error==""){g(".consult-list").prepend(m(A,z,"请耐心等待回复，谢谢",""));B("您的咨询发表成功，请耐心等待回复，谢谢")}else{B(F.error)}})});(new GoTop()).init({pageWidth:960,nodeId:"go-top",nodeWidth:24,distanceToBottom:100,distanceToPage:10,hideRegionHeight:130,text:""})});