package com.example.demo.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.Entity.DongtaiGengzong;
import com.example.demo.Entity.ThreadPoolUtil;
import com.example.demo.Utils.Result;
import org.flowable.engine.*;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("v1/api")

@CrossOrigin
public class DongtaigenzongController {
    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;


    /**
     * 测试接口
     * 是否能ping通
     * */
    @RequestMapping("ping")
    public Result ping() throws Exception {
        
        return Result.sucess();
    }


    /**
     * 值班员提交一个任务
     * @param d 值班员上传的信息对象
     *
     * */
    @PostMapping("create")
    public Result create(DongtaiGengzong d) throws IOException {

        Map<String, Object> map = new HashMap<>();
        map.put("Entity",d);
        runtimeService.startProcessInstanceByKey("dongtai",map);
        return Result.sucess(null);
    }

    /**
     * 获取全部的任务（）
     * 返回任务Id和任务名称
     * */
    @GetMapping("Tasks")
    public Result Tasks()
    {
        List<Task> list = taskService.createTaskQuery().processDefinitionKey("dongtai")
                .list();
        StringBuffer buffer = new StringBuffer();
        for (Task task:
             list) {
            buffer.append(task.getId()+","+task.getName()+";");
        }
        return Result.sucess(buffer);
    }

    /**
     * 班长审核
     * @param taskId 任务id
     * @param userId 班长id
     * @param ok 班长是否同意
     * */
    @GetMapping("banzhang_check")
    public Result banzhang_check(String taskId, String userId, boolean ok, String opinion)
    {
        Map<String, Object> map1 = taskService.getVariables(taskId);
        Map<String, Object> map = new HashMap<>();
        map.put("banzhangId",userId);
        map.put("banzhang_opinion",opinion);
        map.put("a",ok);
        taskService.complete(taskId,map);
        return Result.sucess();
    }
    /**
     * 会审
     * @param taskId 任务id
     * @param userId 会审人员id
     * @param ok 是否通过会审
     * */
    @GetMapping("huishen_check")
    public Result huishen_check(String taskId, String userId, boolean ok, String opinion) throws Exception {
        Map<String, Object> map1 = taskService.getVariables(taskId);
        DongtaiGengzong d1 = (DongtaiGengzong) map1.get("Entity");
        Map<String, Object> map = new HashMap<>();
        map.put("huishenId",userId);
        map.put("huishen_opinion",opinion);
        map.put("c",ok);
        taskService.complete(taskId,map);
        return Result.sucess();
    }
    /**
     * 值班员是否修改
     * @param taskId 任务id
     * @param userId 值班员id
     * @param ok 值班员是否修改
     * */
    @PostMapping("zhibanyuan_check")
    public Result zhibanyuan_check(String taskId, String userId, boolean ok,
                                  DongtaiGengzong gengzong
                                   ) throws Exception {
          if(gengzong.isNUll() && ok)
            throw new Exception("修改需要传入非空的参数");

        Map<String, Object> map1 = taskService.getVariables(taskId);
        if(ok)
        {
            //删除之前的图片和文档
            DongtaiGengzong d1 = (DongtaiGengzong) map1.get("Entity");
            File image = new File(d1.getImage());
            image.delete();

            File doc = new File(d1.getDoc());
            doc.delete();


            String parentPath = d1.getParentPath();
            File f = new File(parentPath);
            f.delete();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("zhibanyuanId",userId);
        map.put("Entity",gengzong);
        map.put("b",ok);
        taskService.complete(taskId,map);
        return Result.sucess();
    }
    /**
     * 会商审核
     * @param taskId 任务id
     * @param userId 会商人员id
     * @param ok 会商是否通过
     * */
    @GetMapping("huishang_check")
    public Result huishang_check(String taskId, String userId, boolean ok)
    {
        Map<String, Object> map1 = taskService.getVariables(taskId);
        Map<String, Object> map = new HashMap<>();
        map.put("huishangId",userId);
        map.put("d",ok);
        taskService.complete(taskId,map);
        return Result.sucess();
    }



/**
 *
 * @author niuyuehua
 * 班长查询自己需要审核的任务
 * 同时返回这些任务的详细信息
 * */
    @GetMapping("/banzhang_query_tasks")
    public Result banzhang_query_tasks()
    {
        List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("dongtai")
                .taskName("banzhangshenhe")
                .orderByTaskCreateTime()
                .desc()
                .list();
      JSONArray array = new JSONArray();
        for (Task task:
                tasks) {
            JSONObject jsonObject = new JSONObject();
            Map<String,Object> variables = taskService.getVariables(task.getId());
            jsonObject.put("taskId",task.getId());
            jsonObject.put("taskName",task.getName());
            jsonObject.put("variables",variables);
            array.add(jsonObject);
        }
        return Result.sucess(array);
    }

    /**
     * @param  id 非必要参数，给的话就是查询某个人的，不给的话就是查询一类人的
     * @param status 表示班长历史审批的状态 true表示通过，false表示不通过
     * 返回班长审批通过的任务，或者是审批不通过的任务（包含用户上传的信息，以及班长审批的意见等）
     * */
    @GetMapping("/banzhang_query_tasks_his")
    public Result banzhang_query_tasks_his(
            @RequestParam(name = "id",required = false,defaultValue = "") String id,
            boolean status)
    {
        List<HistoricVariableInstance> his_list  =  null;
        //先查询班长通过审批的任务或者是没通过审批的任务的实例id
        his_list = historyService.createHistoricVariableInstanceQuery()
            .orderByProcessInstanceId()
            .variableValueEquals("a",status)
            .desc()
            .list();


        DongtaiGengzong dongtaiGengzong = null;//用户上传的信息
        JSONObject jsonObject = null;
        JSONArray jsonArray = new JSONArray();
        //通过实例的id返回历史记录
        for (HistoricVariableInstance historicVariableInstance:
             his_list) {
            List<HistoricVariableInstance> instancesn_list = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(historicVariableInstance.getProcessInstanceId())
                    .orderByProcessInstanceId()
                    .desc()
                    .list();
            jsonObject =  new JSONObject();
            //处理历史记录(封装到json)

            for (HistoricVariableInstance historicVariableInstance1:
            instancesn_list) {
                if(historicVariableInstance1.getVariableName().equals("Entity")||
                        historicVariableInstance1.getVariableName().equals("a")||
                        historicVariableInstance1.getVariableName().equals("banzhangId")||
                        historicVariableInstance1.getVariableName().equals("banzhang_opinion"))
                    jsonObject.put(historicVariableInstance1.getVariableName(),historicVariableInstance1.getValue());
            }
            if(id.equals("")||jsonObject.get("banzhangId").equals(id))
                jsonArray.add(jsonObject);

        }

        return Result.sucess(jsonArray);
    }
    /**
     * @param  id 非必要参数，给的话就是查询某个人的，不给的话就是查询一类人的
     * @param status 表示会审历史审批的状态 true表示通过，false表示不通过
     * 返回会审审批通过的任务，或者是审批不通过的任务（包含用户上传的信息，以及班长审批的意见等）
     * */
    @GetMapping("/huishen_query_tasks_his")
    public Result huishen_query_tasks_his(
            @RequestParam(name = "id",required = false,defaultValue = "") String id,
            boolean status)
    {
        List<HistoricVariableInstance> his_list  =  null;
        //先查询班长通过审批的任务或者是没通过审批的任务的实例id
        his_list = historyService.createHistoricVariableInstanceQuery()
                .orderByProcessInstanceId()
                .variableValueEquals("c",status)
                .desc()
                .list();


        DongtaiGengzong dongtaiGengzong = null;//用户上传的信息
        JSONObject jsonObject = null;
        JSONArray jsonArray = new JSONArray();
        //通过实例的id返回历史记录
        for (HistoricVariableInstance historicVariableInstance:
                his_list) {
            List<HistoricVariableInstance> instancesn_list = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(historicVariableInstance.getProcessInstanceId())
                    .orderByProcessInstanceId()
                    .desc()
                    .list();
            jsonObject =  new JSONObject();
            //处理历史记录(封装到json)

            for (HistoricVariableInstance historicVariableInstance1:
                    instancesn_list) {
                if(historicVariableInstance1.getVariableName().equals("Entity")||
                        historicVariableInstance1.getVariableName().equals("a")||
                        historicVariableInstance1.getVariableName().equals("banzhangId")||
                        historicVariableInstance1.getVariableName().equals("banzhang_opinion")||
                        historicVariableInstance1.getVariableName().equals("c")||
                        historicVariableInstance1.getVariableName().equals("huishenId")||
                        historicVariableInstance1.getVariableName().equals("huishen_opinion")
                )
                    jsonObject.put(historicVariableInstance1.getVariableName(),historicVariableInstance1.getValue());
            }
            if(id.equals("")||jsonObject.get("huishenId").equals(id))
                jsonArray.add(jsonObject);

        }

        return Result.sucess(jsonArray);
    }

    /**
     * @param  id 非必要参数，给的话就是查询某个人的，不给的话就是查询一类人的
     * @param status 表示班长历史审批的状态 true表示通过，false表示不通过
     * 返回班长审批通过的任务，或者是审批不通过的任务（包含用户上传的信息，以及班长审批的意见等）
     * */
    @GetMapping("/huishang_query_tasks_his")
    public Result huishang_query_tasks_his(
            @RequestParam(name = "id",required = false,defaultValue = "") String id,
            boolean status)
    {
        List<HistoricVariableInstance> his_list  =  null;
        //先查询班长通过审批的任务或者是没通过审批的任务的实例id
        his_list = historyService.createHistoricVariableInstanceQuery()
                .orderByProcessInstanceId()
                .variableValueEquals("d",status)
                .desc()
                .list();


        DongtaiGengzong dongtaiGengzong = null;//用户上传的信息
        JSONObject jsonObject = null;
        JSONArray jsonArray = new JSONArray();
        //通过实例的id返回历史记录
        for (HistoricVariableInstance historicVariableInstance:
                his_list) {
            List<HistoricVariableInstance> instancesn_list = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(historicVariableInstance.getProcessInstanceId())
                    .orderByProcessInstanceId()
                    .desc()
                    .list();
            jsonObject =  new JSONObject();
            //处理历史记录(封装到json)

            for (HistoricVariableInstance historicVariableInstance1:
                    instancesn_list) {
                if(historicVariableInstance1.getVariableName().equals("Entity")||
                        historicVariableInstance1.getVariableName().equals("a")||
                        historicVariableInstance1.getVariableName().equals("banzhangId")||
                        historicVariableInstance1.getVariableName().equals("banzhang_opinion")||
                        historicVariableInstance1.getVariableName().equals("c")||
                        historicVariableInstance1.getVariableName().equals("huishenId")||
                        historicVariableInstance1.getVariableName().equals("huishen_opinion")||
                        historicVariableInstance1.getVariableName().equals("d")||
                        historicVariableInstance1.getVariableName().equals("huishangId")||
                        historicVariableInstance1.getVariableName().equals("huishang_opinion")
                )
                    jsonObject.put(historicVariableInstance1.getVariableName(),historicVariableInstance1.getValue());
            }
            if(id.equals("")||jsonObject.get("huishangId").equals(id))
                jsonArray.add(jsonObject);

        }

        return Result.sucess(jsonArray);
    }


    /**
     * @param  id 非必要参数，给的话就是查询某个人的，不给的话就是查询一类人的
     * @param status 表示值班员是否修改的状态 true表示通过，false表示不通过
     * 返回值班员上传的审核通过的或不通过的任务
     * */
    @GetMapping("/zhibanyuan_query_tasks_his")
    public Result zhibanyuan_query_tasks_his(
            @RequestParam(name = "id",required = false,defaultValue = "") String id,
            boolean status)
    {
        List<HistoricVariableInstance> his_list  =  null;
        //先查询班长通过审批的任务或者是没通过审批的任务的实例id
        his_list = historyService.createHistoricVariableInstanceQuery()
                .orderByProcessInstanceId()
                .variableValueEquals("d",status)
                .desc()
                .list();


        DongtaiGengzong dongtaiGengzong = null;//用户上传的信息
        JSONObject jsonObject = null;
        JSONArray jsonArray = new JSONArray();
        //通过实例的id返回历史记录
        for (HistoricVariableInstance historicVariableInstance:
                his_list) {
            List<HistoricVariableInstance> instancesn_list = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(historicVariableInstance.getProcessInstanceId())
                    .orderByProcessInstanceId()
                    .desc()
                    .list();
            jsonObject =  new JSONObject();
            //处理历史记录(封装到json)
            for (HistoricVariableInstance historicVariableInstance1:
                    instancesn_list) {
                if(historicVariableInstance1.getVariableName().equals("Entity")||
                        historicVariableInstance1.getVariableName().equals("a")||
                        historicVariableInstance1.getVariableName().equals("banzhangId")||
                        historicVariableInstance1.getVariableName().equals("banzhang_opinion")||
                        historicVariableInstance1.getVariableName().equals("b")||
                        historicVariableInstance1.getVariableName().equals("zhibanyuanId")||
                        historicVariableInstance1.getVariableName().equals("c")||
                        historicVariableInstance1.getVariableName().equals("huishenId")||
                        historicVariableInstance1.getVariableName().equals("huishen_opinion")||
                        historicVariableInstance1.getVariableName().equals("d")||
                        historicVariableInstance1.getVariableName().equals("huishangId")||
                        historicVariableInstance1.getVariableName().equals("huishang_opinion")
                )
                    jsonObject.put(historicVariableInstance1.getVariableName(),historicVariableInstance1.getValue());
            }
            if(id.equals("")||jsonObject.get("zhibanyuanId").equals(id))
                jsonArray.add(jsonObject);

        }

        return Result.sucess(jsonArray);
    }


    /**
     * 查询有哪些会审的任务
     *
     *
     * */
    @GetMapping("huishen_query_tasks")
    public Result query_all_taskNames()
    {
        List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("dongtai")
                .taskName("huishen")
                .orderByTaskCreateTime()
                .desc()
                .list();
        JSONArray array = new JSONArray();
        for (Task task:
                tasks) {
            JSONObject jsonObject = new JSONObject();
            Map<String,Object> variables = taskService.getVariables(task.getId());
            jsonObject.put("taskId",task.getId());
            jsonObject.put("taskName",task.getName());
            jsonObject.put("variables",variables);
            array.add(jsonObject);
        }
        return Result.sucess(array);
    }


    /**
     * 获取文件（传入一个路径）
     * */
    @GetMapping("download_file")
    public Result download_file(
            HttpServletRequest request,
            HttpServletResponse response,
            String path) throws Exception {

        File f = new File(path);
        InputStream is = new FileInputStream(f);
        byte[] arr = new byte[is.available()];
        is.read(arr);
        is.close();
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename="+f.getName());
        OutputStream os = response.getOutputStream();
        os.write(arr);
        os.flush();
        os.close();
        return Result.sucess();
    }

    @GetMapping("get_image")
    public Result get_iamge(
            HttpServletRequest request,
            HttpServletResponse response,
            String path) throws Exception {
        InputStream is = new FileInputStream(path);
        byte[] arr = new byte[is.available()];
        is.read(arr);
        is.close();
        response.setContentType("image/png");
        OutputStream os = response.getOutputStream();
        os.write(arr);
        os.flush();
        os.close();
        return Result.sucess();
    }

    @GetMapping("huishang_query_tasks")
    public Result huishang_query_tasks()
    {
        List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("dongtai")
                .taskName("huishang")
                .orderByTaskCreateTime()
                .desc()
                .list();
        JSONArray array = new JSONArray();
        for (Task task:
                tasks) {
            JSONObject jsonObject = new JSONObject();
            Map<String,Object> variables = taskService.getVariables(task.getId());
            jsonObject.put("taskId",task.getId());
            jsonObject.put("taskName",task.getName());
            jsonObject.put("variables",variables);
            array.add(jsonObject);
        }
        return Result.sucess(array);
    }

    /**
     * 查询某个值班员需要修改的任务
     * @param Id 值班员的Id
     * 返回该值班员需要修改的任务的链表
     *
     * */
    @GetMapping("zhibanyuan_query_tasks")
    public Result zhibanyuan_query_tasks(String Id)
    {
        List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("dongtai")
                .taskName("zhiabnyuanxiugai")
                .orderByTaskCreateTime()
                .desc()
                .list();
        JSONArray array = new JSONArray();
        for (Task task:
                tasks) {
            Map<String,Object> variables = taskService.getVariables(task.getId());
            DongtaiGengzong entity = (DongtaiGengzong) variables.get("Entity");
            if(entity.getUploadUser().equals(Id))
            {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("taskId",task.getId());
                jsonObject.put("taskName",task.getName());
                jsonObject.put("variables",variables);
                array.add(jsonObject);
            }
        }
        return Result.sucess(array);
    }

}
