<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<%@ page contentType="text/html; charset=UTF-8" %>





<!DOCTYPE html>

<html>
    <head>
        <title></title>
        <meta charset="">
        <link rel="stylesheet" type="text/css" media="screen"   href="public/stylesheets/main.css">
        <link rel="stylesheet" type="text/css" media="screen"   href="public/stylesheets/updateinfo.css">
        <link rel="shortcut icon" type="image/png" href="public/images/favicon.png">
        <script src="public/javascripts/jquery-1.6.4.min.js" type="text/javascript"></script>
    </head>
    <body>
<style type="text/css">
#header {
    height: 123px;
}

.headmain {
    border-bottom: #b73a24 2px solid;
    height: 98px;
    padding-bottom: 10px;
}

.error {
    padding-left: 10px;
    color: red;
}
</style>

<div id="index_main">
    <div id="main">
        <div id="maincontainer" style="overflow: hidden; margin-bottom: 40px; margin-left: 30px;">
            <div class="reg" style="width: 610px; float: left;">
                <div class="reg_phonebd">
                    <p class="ptext">
                        <span class="fcred">注：</span>如您手机已经绑定过优惠啦"优卡"（会员卡），并且首次登录网站请&nbsp;&nbsp;&nbsp;<a
                            href="user.php?act=getpassword&title=2"><img
                            src="public/images/uhl/clickbt.gif" alt=""
                            style="vertical-align: middle" /></a>
                    </p>

                </div>
                <div class="reg_flow-steps">
                    <ol class="num3">

                        <li class="current"><strong class="first">1. 注册新用户</strong></li>

                        <li class="last"><strong>2. 注册成功</strong></li>
                    </ol>
                </div>
                <div class="reg-form">
                    <ul>
                        <li class="field">
                            <div class="input">
                                <label>用户名：</label> <input type="text" name="email" id="email"
                                    value="${params.email}" /> 
                                                                </div>
                            <div class="input"> <span
                                    class="error">#{error 'email' /}</span></div>
                            <div id="showemailcheck" class="msg show-attention">
                                <p class="attention">
                                    请正确填写邮箱，此邮箱作为优惠啦的登录 <br>账号，是找回账户的重要依据。
                                </p>
                            </div>
                        </li>
                        <li class="field">

                            <div class="input">
                                <label>手机：</label><input type="text" name="mobile" id="mobile"
                                    value="${params.mobile}" /> 
                            </div>
                            <div class="input"> <span
                                    class="error">#{error 'mobile' /}</span></div>
                        </li>
                        <li class="field">

                            <div class="input">
                                <label>密码：</label> <input type="password" name="password"
                                    id="password" value="${params.password}" /> 
                            </div>
                            <div class="input"> <span
                                    class="error">#{error 'password' /}</span></div>
                            <div class="msg show-attention">
                                <p class="attention">密码6-20个数字，请使用单独数字生成的密码，不能使用字母或者其他字符。</p>
                            </div>

                        </li>
                        <li class="field">
                            <div class="input">
                                <label>确认密码：</label> <input type="password" name="sure_pwd"
                                    id="sure_pwd" value="${params.sure_pwd}" />
                            </div>
                            <div class="input"> <span
                                    class="error">#{error 'sure_pwd' /}</span></div>
                            <div class="msg show-attention">
                                <p class="attention" style="margin-top: 5px;">请再输入一遍您上面填写的密码</p>
                            </div>
                        </li>
                        <li class="field">
                            <div class="input">
                                <label>验证码：</label><input type="captcha" id="captcha"
                                    name="captcha" class="{required:true,maxlength:4}" />
                            </div>
                                <div><span
                                    class="error">#{error 'captcha' /}</span></div>
                    </li>
                    <li class="field">
                    <div class="input" align="center">
                        <img
                                    id="captchaImg" style="width: 100px; height: 30px;"
                                    src="/captcha?randomID=${randomID}"> 
                                    <a id="captchaLink" name="captchaLink" href="javascript:void(0);">换一个</a>
                                     <input type="hidden" name="randomID" value="${randomID}" />
                        </div>
                    </li>
                        <li class="submit-field">
                            <div class="input">
                                <button id="regbutton" type="submit">同意以下服务条款，立即注册</button>
                            </div>
                        </li>
                        <li class="notreg-field">
                            <div class="input">
                                已有账号？<a href="user.php?act=login">现在登录</a><img
                                    src="images/uhl/loginbt.gif" style="vertical-align: middle" />
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
            <form:form method="post" id="fm1"  commandName="${commandName}" htmlEscape="true">
                  <form:errors path="*" id="msg" cssClass="errors" element="div" />
            <div style="margin: 145px 18pt 0pt 10px;" class="loginbg">
                <strong style="font-size: 14px; margin-bottom: 6px; display: block;">登录</strong>
                <div class="login">
                    <ul class="loginUl">
                        <li class="field" id="showmess">
                            <div class="pwderror">
                                <h1 class="colorred"></h1>

                            </div>
                        </li>
                        <li class="field">
                            <div class="input">
                                <label>用户名：</label> 
                                <form:input  cssErrorClass="error" id="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="false" htmlEscape="true" />
                            </div>
                        </li>

                        <li class="field">
                            <div class="input">
                                <label>密码：</label> 
                                <form:password  cssErrorClass="error" id="password" size="25" tabindex="2" path="password"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
                            </div>
                        </li>
                        <li id="showcode1" class="field" style="display: none;">
                            <div class='input showcode_1'>

                                <b> <label class='label1'>验证码：</label> <input type='text'
                                    size='4' maxlength='4' id='login_captcha' name='login_captcha'
                                    tabindex='7' class='input1'> <img id='codesrc'
                                    src='registernum.php' style='cursor: pointer;'
                                    onClick="this.src='registernum.php?rand='+Math.random()" /> <span
                                    id='messcheckcode'></span>
                                </b> <b> <span>看不清？</span> <span id='changecode'
                                    onClick="$('#codesrc').attr('src', 'registernum.php?rand='+Math.random())"
                                    class='colorblue'>换一张</span>
                                </b>
                            </div>
                        </li>
                        <li class="submit-field">
                        <input type="hidden" name="lt" value="${loginTicket}" />
                        <input type="hidden" name="execution" value="${flowExecutionKey}" />
                        <input type="hidden" name="_eventId" value="submit" />

                        <button id="loginbutton" type="submit" 
                                class="bt colorw bold">登录</button> <a
                            href="user.php?act=forgetpw&logintrade=1">忘记密码</a>
                </li>
                <li class="fieldnot"><span>还没有优惠啦账号?</span> <span
                    class="colorred">&nbsp;&nbsp;&nbsp;&nbsp;</span></li>

                </ul>
                <div class="clear"></div>
            </div>
            </form:form>
            
            <p class="loginfj">
                <span class="colorred">注：</span>如您手机已经绑定过优惠啦"优卡"(会员卡)，<br>
                &nbsp;&nbsp;&nbsp;&nbsp;并且首次登录网站请<a href="#">点击</a>
            </p>
        </div>
    </div>

    <div class="agreements">
        <ul>
            <li>
                <div class="acontent">
                    <p>
                        <strong>优惠啦网站服务条款</strong><br />
                    <p>

                        优惠啦网站（http://www.uhuila.com/）所提供的各项服务的所有权和运作权归视惠信息（上海）科技有限公司。用户必须同意下述所有服务条款并完成注册程序，才能成为优惠啦网站的正式会员并使用优惠啦提供的各项服务。服务条款的修改权归视惠信息（上海）科技有限公司所有。用户应当随时关注本服务条款的修改，并决定是否继续使用本网站提供的各项服务。用户登录并使用优惠啦网站各项服务的行为，将被视为用户对当时的用户注册协议的同意和承诺遵守。<br />

                        商户用户还须同意及遵守《商户服务条款》。<br />

                        一、优惠啦网站运用自己的操作系统，通过国际互联网络等手段为会员提供消费信息、优惠信息、电子刊物等网络服务。优惠啦网站保留随时修改或中断服务而不需知照会员的权利。优惠啦行使修改或中断服务的权利，不需对会员或第三方负责。<br />

                        视惠信息（上海）科技有限公司会在必要时修改服务条款，优惠啦网站会员服务条款一旦发生变动，公司将会在用户进入下一步使用前的页面提示修改内容。如果你同意改动，则点击"同意服务条款"按钮。如果你不接受，则及时取消你的用户使用服务资格。
                        <br /> 用户要继续使用优惠啦会员服务需要两方面的确认：<br /> （1）首先确认优惠啦服务条款及其变动。<br />

                        （2）同意接受所有的服务条款限制。<br /> 二、用户必须自行准备如下设备和承担如下开支：<br />
                        （1）上网设备，包括并不限于电脑或者其他上网终端、调制解调器及其他上网装置。<br />
                        （2）上网开支，包括并不限于网络接入费、上网设备租用费等；<br /> 三、保护会员隐私权<br />
                        本协议所称之会员隐私包括被法律确认为隐私内容，并符合下述范围的信息：<br /> 您注册优惠啦网站时，根据网站要求提供的个人信息；<br />
                        在您使用优惠啦服务、参加网站活动、或访问网站网页时，网站自动接收并记录的您浏览器上的服务器数据，包括但不限于IP地址、网站Cookie中的资料及您要求取用的网页记录；<br />
                        优惠啦网站通过合法途径从商业伙伴处取得的用户个人资料。<br />


                        优惠啦网站不会向任何人出售或出借您的个人信息，除非事先得到您的许可。<br />
                        为服务用户的目的，优惠啦可能通过使用您的个人信息，向您提供服务，包括但不限于向您发出活动和服务信息等。用户在享用优惠啦会员服务的同时，同意接受优惠啦网络会员服务提供的各类信息服务。<br />
                        优惠啦网站承诺不公开或透露您的密码、姓名、手机号码等在本站的非公开信息，除非因会员本人的需要、法律或其他合法程序的要求、服务条款的改变或修订等。
                        您的个人信息将在下述情况下部分或全部被披露：<br /> ● 经您同意，向第三方披露；<br /> ●
                        如您是合格的知识产权人并已提起投诉，应被投诉人要求，向被投诉人披露，以便双方处理可能的权利纠纷；<br /> ●
                        根据法律的有关规定，或者行政或司法机构的要求，向第三方或者行政、司法机构披露；<br /> ●
                        如果您出现违反中国有关法律或者网站政策的情况，需要向第三方披露；<br /> ●
                        为提供你所要求的产品和服务，而必须和第三方分享您的个人信息；<br /> ● 其他本网站根据法律或者网站政策认为合适的披露。<br />


                        同时会员须做到：<br /> ● 用户名和昵称的注册与使用应符合网络道德，遵守中华人民共和国的相关法律法规。<br /> ●
                        用户名和昵称中不能含有威胁、淫秽、漫骂、非法、侵害他人权益等有争议性的文字。<br /> ●
                        注册成功后，会员必须保护好自己的帐号和密码，因会员本人泄露而造成的任何损失由会员本人负责。另外，每个用户都要对其帐户中的所有活动和事件负全责。你可随时改变你的密码和图标，也可以结束旧的帐户重开一个新帐户。用户若发现任何非法使用用户帐号或安全漏洞的情况，应当立即通告优惠啦网站。
                        <br /> ● 不得盗用他人帐号，由此行为造成的后果自负。<br /> 四、责任说明<br />
                        基于技术和不可预见的原因而导致的服务中断，或者因会员的非法操作而造成的损失，优惠啦网站不负责任。会员应当自行承担一切因自身行为而直接或者间接导致的民事或刑事法律责任。<br />

                        五、会员必须做到：<br />
                        1、不得利用本站危害国家安全、泄露国家秘密，不得侵犯国家社会集体的和公民的合法权益，不得利用本站制作、复制和传播下列信息：<br />

                        （1）煽动抗拒、破坏宪法和法律、行政法规实施的；<br /> （2）煽动颠覆国家政权，推翻社会主义制度的；<br />
                        （3）煽动分裂国家、破坏国家统一的；<br /> （4）煽动民族仇恨、民族歧视，破坏民族团结的；<br />

                        （5）捏造或者歪曲事实，散布谣言，扰乱社会秩序的；<br />
                        （6）宣扬封建迷信、淫秽、色情、赌博、暴力、凶杀、恐怖、教唆犯罪的；<br />
                        （7）公然侮辱他人或者捏造事实诽谤他人的，或者进行其他恶意攻击的；<br /> （8）损害国家机关信誉的；<br />
                        （9）其他违反宪法和法律行政法规的；<br /> （10）进行商业广告行为的。<br />
                        2、未经本站的授权或许可，任何会员不得借用本站的名义从事任何商业活动，也不得将本站作为从事商业活动的场所、平台或其他任何形式的媒介。禁止将本站用作从事各种非法活动的场所、平台或者其他任何形式的媒介。如会员违反上述规定，则优惠啦网站有权直接采取一切必要的措施，包括但不限于删除会员发布的内容、取消会员在网站获得的星级、荣誉以及虚拟财富，暂停或查封会员帐号，乃至通过诉讼形式追究会员法律责任等。
                        3、优惠啦网站保留经自行裁决和判断而过滤、编辑或移除任何上述内容的权利。<br />
                        优惠啦网站对您或任何第三方发布、存储或上传的任何内容或其任何损失或损害，均不负责也不承担责任，对您可能遇到的任何错误、中伤、诽谤、诬蔑、不作为、谬误、淫秽、色情或亵渎，优惠啦网站也不承担责任。作为互动服务的提供者，优惠啦网站对其用户在任何公共论坛、个人主页或其它互动区域提供的任何陈述、声明或内容均不承担责任。优惠啦网站保留在任何时候为任何理由而不经通知地移除、筛查或编辑本网站上发布或存储的任何内容的权利，且其具有绝对的裁量权如此行为，您须自行负责备份和替换您在本网站发布或存储的任何内容，成本和费用自理。<br />
                        4、同意并遵守《优惠啦用户诚信公约及违规行为处理流程》。<br /> 六、会员权利：<br /> ●
                        会员有权在注册并登陆后对站内商户发布客观、真实的图片和点评信息，而无需向优惠啦支付平台使用费；<br /> ●
                        会员有权在注册并登陆后帮助优惠啦网站完善站内商户的营业时间、交通等信息，而无需向优惠啦支付平台使用费；<br /> ●
                        会员有权根据网站政策在作出发布点评信息等内容贡献后取得优惠啦网站给与的奖励；<br /> ●
                        会员有权修改其个人账户中各项可修改信息，自行选择昵称和录入介绍性文字，自行决定是否提供非必填项的内容；<br /> ●
                        会员有权参加优惠啦网站社区，发表符合国家规定和优惠啦网站社区规则的观点；<br /> ●
                        会员有权根据根据网站政策在优惠啦网站社区通过贡献内容获得优惠啦网站给与的奖励；<br /> ●
                        会员有权参加优惠啦网站社区组织提供的各项线上、线下活动；<br /> ●
                        会员在取得优惠啦网站发放的会员卡后，有权根据所在城市会员卡服务类别在会员卡商户获得折扣或积分等利益。<br /> 七、版权说明：
                        任何会员接受本注册协议，即表明该用户主动将其在任何时间段在本站发表的任何形式的信息的著作财产权，包括并不限于：复制权、发行权、出租权、展览权、表演权、放映权、广播权、信息网络传播权、摄制权、改编权、翻译权、汇编权以及应当由著作权人享有的其他可转让权利无偿独家转让给优惠啦网站运营商所有，同时表明该会员许可优惠啦网站有权利就任何主体侵权而单独提起诉讼，并获得全部赔偿。
                        本协议已经构成《著作权法》第二十五条所规定的书面协议，其效力及于用户在优惠啦网站发布的任何受著作权法保护的作品内容，无论该内容形成于本协议签订前还是本协议签订后。<br />
                        会员同意并明确了解上述条款，不将已发表于本站的信息，以任何形式发布或授权其它网站（及媒体）使用。<br />
                        同时，优惠啦网站保留删除站内各类不符合规定点评而不通知会员的权利：<br />
                        视惠信息（上海）科技有限公司是优惠啦网站的制作者,拥有此网站内容及资源的版权,受国家知识产权保护,享有对本网站声明的最终解释与修改权；未经视惠信息（上海）科技有限公司的明确书面许可,任何单位或个人不得以任何方式,以任何文字作全部和局部复制、转载、引用和链接。否则本公司将追究其法律责任。会员已经明确阅读了并同意本网站的《知识产权声明》。<br />

                        八、免责声明：<br /> ●
                        优惠啦网站对于任何包含、经由或连接、下载或从任何与有关本网站所获得的任何内容、信息或广告，不声明或保证其正确性或可靠性；并且对于用户经本网站上的内容、广告、展示而购买、取得的任何产品、信息或资料，优惠啦网站不负保证责任。用户自行负担使用本网站的风险。<br />
                        ● 优惠啦网站有权但无义务，改善或更正本网站任何部分之任何疏漏、错误。<br /> ●
                        本站内会员点评仅代表其个人的观点，并不表示本站赞同其观点或证实其描述，本站不承担由此引发的法律责任。任何单位或者个人认为本站用户发布的任何信息侵犯其权利，均可以通过下列联系方式通知优惠啦网站：<br />
                        邮寄地址：上海市徐汇区桂平路291号2楼<br /> 收件人：视惠信息（上海）科技有限公司<br />
                        客服电话：400-6262-166<br /> 敬请按照以下格式（包括各项编号）提供下述信息： <br />
                        明确指出您声称被侵权的受版权保护的作品。 <br />
                        明确指出您声称侵犯受版权保护作品的材料，以及使我们能在本网站上找到该材料的信息，例如一个指向侵权材料的链接。
                        您的联络方式，以便我们能回复您的投诉，最好包括电子邮件地址和电话号码。 <br />

                        纳入以下声明："本人本着诚信原则，认为被指侵犯版权的材料未获得版权所有人、其代理或法律的授权"。<br />
                        纳入以下声明："本人宣誓，此通知里的信息是准确的，本人是声称被侵犯的排他性权利的版权所有人，或获得授权代表该权利所有人行为，所言不实甘受伪誓处罚"。<br />
                        通知必须经被授权人签字，该被授权人是代表声称被侵犯的排他性权利的所有人而行为。<br />
                        我们建议您在提起通知或通知应答之前咨询您的法律顾问。此外，我们提请您注意：如果对版权侵权所作的指称不实，则您可能承担损害赔偿（包括各种费用和律师费）的责任。<br />


                        九、侵权者政策<br /> 对于被视为侵犯他人知识产权的任何用户，优惠啦网站可自行决定限制其对本网站的访问和／或终止其帐户。<br />

                        十、保证否认声明<br />
                        除非优惠啦网站以书面形式明确另行规定，否则本网站、其中所包含的材料以及本网站上提供的或与之相关而提供的服务（"服务"）均以"现状"提供，不带任何类型的保证，无论明示还是默示。就本网站上的服务、信息、内容以及材料，优惠啦网站明确否认所有其它明示或默示的保证，包括但不限于有关适销性、适合特定目的、所有权以及不侵权的默示保证。优惠啦网站不声明或保证本网站的材料或服务是准确的、完整的、可靠的、当前的或无差错的，并且否认有关本网站、本网站内容或其任何部分的准确性或专有性的任何保证或声明。<br />
                        优惠啦网站对与定价、文本或摄影有关的排字错误或疏忽不负责。虽然优惠啦网站力图使您能对本网站和服务进行安全访问和使用，但优惠啦网站不能也不会声明或保证本网站或其服务器是不含病毒或其它有害因素的；因此您应使用业界公认的软件查杀任何下载文件中的病毒。<br />


                        十一、适用法律和裁判地点<br />
                        本网站（第三方网站除外）由视惠信息（上海）科技有限公司控制并运营。本网站可在中国及其他国家进行访问。您和视惠信息（上海）科技有限公司均受益于与本网站有关的可预见法律环境的建立。因此，您和视惠信息（上海）科技有限公司明确同意，因您使用本网站而引起或与之相关的一切争议、权利主张或其它事项，均受中华人民共和国法律的管辖，但不考虑其法律冲突原则。您同意，如果出现任何因本网站引起或与之相关的争议，您和视惠信息（上海）科技有限公司应首先本着诚信原则通过协商加以解决。如果协商不成，您将同意接受视惠信息（上海）科技有限公司住所地法院管辖。
                        <br /> 十二、可分性<br />
                        如果本网站条款的任何规定被视为不合法、无效或因任何原因而无法执行，则此等规定应视为可从本网站条款分割，且不得影响任何其余规定的效力和可执行性。

                        <br /> 十三、冲突选择<br />
                        本协议是优惠啦网站与用户之间的法律关系的重要文件，优惠啦网站或者注册用户的任何书面或者口头意思表示与本协议不一致的，均应当与本协议为准，除非本协议被优惠啦网站声明作废或者被新版本代替。
                        <br /> 十四、问题与意见<br /> 如果您对本网站条款或本网站的使用还存有任何疑问，您可以联系我们。<br />

                        十五、针对搜索引擎<br /> 优惠啦网站要求各搜索引擎遵循行业规范，即"拒绝 Robots 访问标准"(Robots
                        Exclusion Standard)，否则将视你的抓取行为是对我网站财产权利和知识产权的侵犯，有权通过法律诉讼维护网站利益。<br />


                    </p>

                </div>

            </li>
        </ul>
    </div>
</div>
        
    </body>
</html>


