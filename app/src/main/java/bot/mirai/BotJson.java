package bot.mirai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;

public class BotJson{
    public JSONObject jsonObj=null;
    BotJson(){};
    BotJson(String in){
        //System.out.println(in);
        jsonObj = JSON.parseObject(in);
    }
    BotJson(JSONObject in){
        jsonObj = in;
    }

    void put(String key,String value){
        try{
            jsonObj.put(key,value);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    JSONObject get(String key){
        try{
            return ((JSONObject)jsonObj.get(key));
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    JSONObject getJson(){
        if(jsonObj != null){
            return jsonObj;
        }
        return null;
    }

    String ret(){
        String target = "{\"sha\":\"ec8b68c75ffd7c96a53cd0b572351177d74e6151\",\"node_id\":\"MDY6Q29tbWl0MzUxMTE3MjUyOmVjOGI2OGM3NWZmZDdjOTZhNTNjZDBiNTcyMzUxMTc3ZDc0ZTYxNTE=\",\"commit\":{\"author\":{\"name\":\"Yinlianlei\",\"email\":\"1114561520@qq.com\",\"date\":\"2021-07-07T06:10:35Z\"},\"committer\":{\"name\":\"Yinlianlei\",\"email\":\"1114561520@qq.com\",\"date\":\"2021-07-07T06:10:35Z\"},\"message\":\"update\",\"tree\":{\"sha\":\"ff8e8cb8e2fb6a1466918b59a4db024fc1d49eae\",\"url\":\"https://api.github.com/repos/Yinlianlei/Bot/git/trees/ff8e8cb8e2fb6a1466918b59a4db024fc1d49eae\"},\"url\":\"https://api.github.com/repos/Yinlianlei/Bot/git/commits/ec8b68c75ffd7c96a53cd0b572351177d74e6151\",\"comment_count\":0,\"verification\":{\"verified\":false,\"reason\":\"unsigned\",\"signature\":null,\"payload\":null}},\"url\":\"https://api.github.com/repos/Yinlianlei/Bot/commits/ec8b68c75ffd7c96a53cd0b572351177d74e6151\",\"html_url\":\"https://github.com/Yinlianlei/Bot/commit/ec8b68c75ffd7c96a53cd0b572351177d74e6151\",\"comments_url\":\"https://api.github.com/repos/Yinlianlei/Bot/commits/ec8b68c75ffd7c96a53cd0b572351177d74e6151/comments\",\"author\":{\"login\":\"Yinlianlei\",\"id\":57148635,\"node_id\":\"MDQ6VXNlcjU3MTQ4NjM1\",\"avatar_url\":\"https://avatars.githubusercontent.com/u/57148635?v=4\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/Yinlianlei\",\"html_url\":\"https://github.com/Yinlianlei\",\"followers_url\":\"https://api.github.com/users/Yinlianlei/followers\",\"following_url\":\"https://api.github.com/users/Yinlianlei/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/Yinlianlei/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/Yinlianlei/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/Yinlianlei/subscriptions\",\"organizations_url\":\"https://api.github.com/users/Yinlianlei/orgs\",\"repos_url\":\"https://api.github.com/users/Yinlianlei/repos\",\"events_url\":\"https://api.github.com/users/Yinlianlei/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/Yinlianlei/received_events\",\"type\":\"User\",\"site_admin\":false},\"committer\":{\"login\":\"Yinlianlei\",\"id\":57148635,\"node_id\":\"MDQ6VXNlcjU3MTQ4NjM1\",\"avatar_url\":\"https://avatars.githubusercontent.com/u/57148635?v=4\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/Yinlianlei\",\"html_url\":\"https://github.com/Yinlianlei\",\"followers_url\":\"https://api.github.com/users/Yinlianlei/followers\",\"following_url\":\"https://api.github.com/users/Yinlianlei/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/Yinlianlei/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/Yinlianlei/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/Yinlianlei/subscriptions\",\"organizations_url\":\"https://api.github.com/users/Yinlianlei/orgs\",\"repos_url\":\"https://api.github.com/users/Yinlianlei/repos\",\"events_url\":\"https://api.github.com/users/Yinlianlei/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/Yinlianlei/received_events\",\"type\":\"User\",\"site_admin\":false},\"parents\":[{\"sha\":\"e50f95e8d58fb017f8d367804e64114a86fb2555\",\"url\":\"https://api.github.com/repos/Yinlianlei/Bot/commits/e50f95e8d58fb017f8d367804e64114a86fb2555\",\"html_url\":\"https://github.com/Yinlianlei/Bot/commit/e50f95e8d58fb017f8d367804e64114a86fb2555\"}]}";
        JSONObject re = JSON.parseObject(target);
        re = (JSONObject)re.get("commit");
        System.out.println(re.toJSONString());
        re = (JSONObject)re.get("committer");
        System.out.println(re.toJSONString());
        System.out.println(re.get("date"));
        return (String)re.get("date");
    }

    String getString(){
        return JSON.toJSONString(jsonObj);
    }
}