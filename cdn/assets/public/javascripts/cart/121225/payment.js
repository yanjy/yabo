jQuery(function(d){var a=0,f=0,h=Number(d("#j_total-money").text()),c=Number(d("#balance").text()),i=d("#j_selected-voucher"),j=d("#payment"),e=d("#other-pay");d("#j_voucher").delegate("a, input","click",function(){var k=d(this);if(k.hasClass("use")){d(".voucher-box").show()}else{if(k.attr("data-facevalue")){if(f>=h){if(k.attr("checked")=="checked"){k.attr("checked",false);i.html("已选择<em>"+a+"</em>张抵用券，抵扣<em>"+f+"</em>元，不要再点了哦，再点就浪费了！")}else{a--;f-=Number(k.attr("data-facevalue"));if(a==0){i.html("请选择要使用的抵用券")}else{i.html("已选择<em>"+a+"</em>张抵用券，抵扣<em>"+f+"</em>元")}}}else{if(k.attr("checked")=="checked"){a++;f+=Number(k.attr("data-facevalue"));i.html("已选择<em>"+a+"</em>张抵用券，抵扣<em>"+f+"</em>元")}else{a--;f-=Number(k.attr("data-facevalue"));if(a==0){i.html("请选择要使用的抵用券")}else{i.html("已选择<em>"+a+"</em>张抵用券，抵扣<em>"+f+"</em>元")}}}g()}}});function g(){var k=h-f,l=k-c;if(k>0){if(l>0){k=c}else{l="0.00"}j.slideDown(50);b()}else{k="0.00";l="0.00";j.slideUp(50);e.slideUp(50)}d("#balance-pay").text(k);d("#online-pay").text(l)}d("#use_balance").click(function(){b()});function b(){if(d("#use_balance").attr("checked")=="checked"){if(f+c>=h){e.slideUp(50)}else{e.slideDown(50)}}else{e.slideDown(50)}}d("#confirm").click(function(){var k=d("input[name=paymentSourceCode]:checked");if(f<h){if(d("#use_balance").attr("checked")=="checked"){if(f+c<h){if(k.length==0){d("#message").text("请选择支付方式");return false}}}else{if(k.length==0){d("#message").text("请选择支付方式");return false}}}})});