package com.tadevelop.sdk;

public interface ServerUrl {

	/**
	 * 2.1 版本升级
	 */
	public static final String GET_UPGRADE = "rest/com/upgrade/";

	/**
	 * 2.4 用户登陆
	 */
	public static final String POST_LOGIN = "rest/login/getAccessToken";

	/**
	 * 2.2 获取短信验证码
	 */
	public static final String POST_GETCHECKCODE = "rest/login/getCheckCode";

	/**
	 * 2.3 用户注册
	 */
	public static final String POST_USERREGISTER = "rest/login/userRegister";

	/**
	 * 重置密码
	 */
	public static final String POST_RESETPASSWORD = "rest/login/resetPassword";

	/**
	 * 2.8 添加用户详细信息
	 */
	public static final String POST_USERPROFILE = "rest/login/userInfoRegister";

	/**
	 * 2.9 查询用户的详细信息
	 */
	public static final String GET_USERPROFILE = "rest/userInfo/getUserProfile";

	/**
	 * 2.22 查询所有用户好友发布的微博和转发的微博
	 */
	public static final String GET_SELECTMESSAGE = "rest/messages/selectMessages";

	/**
	 * 2.18 查询登陆用户的所有新好友
	 */
	public static final String GET_SELECTALLFRIENDS = "rest/relation/selectAllFriends";

	/**
	 * 2.16 查询登陆用户的所有关注好友
	 */
	public static final String GET_SELECTATFRIENDS = "rest/relation/listAtFriend";

	public static final String GET_FINDADDRESS = "rest/relation/findAddress";

	/**
	 * 2.25 用户发表微博
	 */
	public static final String POST_WRITEMESSAGES = "rest/messages/insterMessages";

	/**
	 * 查询所有用户关注的人发布的微博和转发的微博
	 */
	public static final String GET_SELECTFANMESSAGE = "rest/messages/selectFanMessages";

	/**
	 * 查询用户自己发布微博和转发的微博
	 */
	public static final String GET_SELECTMEMESSAGE = "rest/messages/selectMySelfMessages";

	/**
	 * 2.26用户转发微博
	 */
	public static final String POST_TRANSPONDMESSAGES = "rest/messages/transpondMessages";

	/**
	 * 2.27用户评论微博
	 */
	public static final String POST_COMMENTMESSAGES = "rest/messages/commentMessages";

	/**
	 * 2.29用户赞一个
	 */
	public static final String POST_PRAISE = "rest/messages/praise";

	/**
	 * 修改用户的详细信息
	 */
	public static final String POST_UPDATEUSERINFO = "rest/userInfo/changeUserProfile";

	/**
	 * 根据用户所输入的信息查找用户的详细信息显示2度人脉以内的
	 */
	public static final String GET_SEARCHFRIEND = "rest/relation/searchFriend";

	/**
	 * 根据用户所输入的信息查找用户的详细信息显示2度人脉以内的getRecommendFriend
	 */
	public static final String POST_ADDFRIEND = "rest/relation/addFriend";

	/**
	 * 推荐2度人脉的好友(登录者的userId)
	 */
	public static final String GET_RECOMMENDFRIEND = "rest/relation/getRecommendFriend";

	/**
	 * 删除关系
	 */
	public static final String DELETE_DELETE = "rest/relation/delete";

	/**
	 * 删除通讯录好友
	 */
	public static final String DELETE_DELETEADDRESS = "rest/relation/deleteAddress";

	/**
	 * 删除自己的微博或转发的微博
	 */
	public static final String DELETE_MESSAGE = "rest/messages/DeleteMessages";

	/**
	 * 查看单条微博的所有评论
	 */
	public static final String GET_SELECTCOMMENTMESSAGE = "rest/messages/selectcommentMessages";

	/**
	 * 他的好友(userId为登录用户)
	 */
	public static final String GET_USERFRIENDS = "rest/relation/getUserFriends";

	/**
	 * 查询某个用户的发布微博和转发的微博(userId为某个用户)
	 */
	public static final String GET_SELECTUSERMESSAGES = "rest/messages/selectUserMessages";

	/**
	 * 刷新手机令牌
	 */
	public static final String POST_REFRESHTOKEN = "rest/login/refreshToken";

	/**
	 * 查询关系状态
	 */
	public static final String GET_SELECTUSERINFO = "rest/relation/selecteUserInfo";

	/**
	 * 添加用户关注
	 */
	public static final String POST_ATFRIEND = "rest/relation/atFriend";

	/**
	 * 删除微博评论
	 */
	public static final String DELETE_DELETECOMMENTMSG = "rest/messages/DeleteCommentMsg";

	/**
	 * 修改密码
	 */
	public static final String POST_CHANGEPASSWORD = "rest/login/changePassword";

	/**
	 * 保存通讯录到服务器
	 */
	public static final String POST_SAVEADDRESSBOOK = "rest/addressbook/saveAddressBook";

	/**
	 * 上传通讯录
	 */
	public static final String POST_CONTACTS_UPLOAD = "rest/contacts/upload";

	/**
	 * 用户提交反馈
	 */
	public static final String POST_FEEDBACK_SUBMIT = "rest/feedback/submit";

	/**
	 * 拉取职业
	 */
	public static final String GET_ALLJOB = "rest/job/allJob";

	/**
	 * 好友关系
	 */
	public static final String GET_RELATIONSHIP = "rest/relation/relationship";

	/**
	 * 获取用户简要信息
	 */
	public static final String GET_USER_PROFILE_LITTLE = "rest/user/info/little";
}
