//发送订单数量变更请求，若数量变为非正整数，则视为删除
function reorder(goods_id,indent){
    var element = $("#num_" + goods_id);
    var last_num = $("#last_num_" + goods_id);

    var new_num = Number(last_num.val()) + indent;
    if(new_num <= 0){
        $.ajax({
            type:'DELETE',
            url:'/carts/' + goods_id,
            success:function(data){
                $("#row_" + goods_id).remove();
                refreshAmount();
            }});
        return;
    }
    $.post('/carts',
            {goodsId:goods_id,number:indent},
            function(data){
                element.val(new_num);
                last_num.val(new_num);
                calItem(goods_id);
                refreshAmount();
            });
}
//计算单行的总价
function calItem(goods_id){
    var total_price = Number($("#price_" + goods_id).text()) * Number($("#num_" + goods_id).val());
    $("#subtotal_" + goods_id).val(total_price);
}
//计算订单总价
function refreshAmount(){
    var number = 0;
    $("input.num_input").each(function(){number += Number($(this).val())});
    $("#total_num").text(number);

    var amount = 0;
    $("input[id^=subtotal_]").each(function(){amount += Number($(this).val())});;
    $("#carts_amount").text(amount);
}

$(window).load(
    function(){
        //点击+按钮
        $("a.add_box").click(function() {
            reorder($(this).attr("name"),1);
            return false;
        });
        //点击-按钮
        $("a.reduce_box").click(function() {
            reorder($(this).attr("name"),-1);
            return false;
        });
        //直接在文本框里输入
        $("input.num_input").blur(function(){
            var el_id = $(this).attr("id");
            var last_num = $("#last_" + el_id);
            var re= /^\d+(\.\d+)?$/;
            if(!re.test($(this).val())){
                $(this).val(last_num.val());
                return;
            }
            var goods_id = el_id.substr(el_id.lastIndexOf("_") + 1);

            reorder(goods_id, Number($(this).val()) - Number(last_num.val()));
        });
        //点击删除
        $("a.delete_gift").click(function(){  
            var goods_id = $(this).attr("name");
            $.ajax({
                type:'DELETE',
                url:'/carts/' + goods_id,
                success:function(data){
                    $("#row_" + goods_id).remove()
                }});
            
            return false;
        });

        var set_all_select_all_checkbox =function(checked){
            $("input[name=select_all_checkbox]").each(function(){
                this.checked = checked
            });
        };
        var set_all_goods_checkbox = function(checked){
            $("input[id^=check_goods_]").each(function(){
                this.checked = checked
            }); 
        };
        //点击全选
        $("input[name=select_all_checkbox]").each(function(){
               $(this).click( 
                  function(){ 
                     if(this.checked){ 
                        set_all_select_all_checkbox(true);
                        set_all_goods_checkbox(true);
                     }else{ 
                        set_all_select_all_checkbox(false);
                        set_all_goods_checkbox(false);
                     } 
                  } 
                )});
        //点击单个复选框
        var all_checked= function(){
            var all_check = true;
            $("input[id^=check_goods_]").each(function(){
                if(this.checked){
                    return true;
                }else{
                    all_check = false;
                    return false;
                }
            }); 

            return all_check;
        };
        $("input[id^=check_goods_]").each(function(){ 
            $(this).click(
                function(){
            if(all_checked()){
                set_all_select_all_checkbox(true);
            }else{
                set_all_select_all_checkbox(false);
            }
        })}); 


        //点击批量删除
        $("#batch_delete").click(function(){
            var checked=[]
            $("input[id^=check_goods_]").each(function(){
                if(this.checked){
                    var id_temp = $(this).attr("id");
                    checked.push(id_temp.substr(id_temp.lastIndexOf("_")+1));
                }
            });

            if(checked.length == 0) return;

            $.ajax({
                type:'DELETE',
                url:'/carts/' +checked.join(","),
                success:function(data){
                    for(var i = 0; i< checked.length; i++){
                        $("#row_" + checked[i]).remove();
                    }
                    set_all_select_all_checkbox(false);
                    refreshAmount();
                }});
            
            });
        refreshAmount();
    }
);

