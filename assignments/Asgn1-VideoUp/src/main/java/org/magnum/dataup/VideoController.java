package org.magnum.dataup;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoController {
	
	private VideoFileManager videoDataMgr;
    private static final AtomicLong currentId = new AtomicLong(0L);
	private Map<Long,Video> videos = new HashMap<Long, Video>();

	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return videos.values();
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		save(v);
		v.setDataUrl(getDataUrl(v.getId()));
        return v;
		
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id, @RequestParam(VideoSvcApi.DATA_PARAMETER) MultipartFile videoData, HttpServletResponse response) {
		VideoStatus state = new VideoStatus(VideoState.PROCESSING);
		if (videos.containsKey(id)) {
			try {
				saveSomeVideo(videos.get(id), videoData);
			} catch (IOException e) {
				e.printStackTrace();
			}         
		}
		else {
			response.setStatus(404);
		}
			
		state.setState(VideoState.READY);
		return state;
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.GET)
	public @ResponseBody void getData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id, HttpServletResponse response) {
		try {
			if (videos.containsKey(id)) {
				serveSomeVideo(videos.get(id), response);
			}
			else {
				response.setStatus(404);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Video save(Video entity) {
        checkAndSetId(entity);
        videos.put(entity.getId(), entity);
        return entity;

	}
	
	private void checkAndSetId(Video entity) {
        if(entity.getId() == 0){
        	entity.setId(currentId.incrementAndGet());
        }
	}
	
 	private String getUrlBaseForLocalServer() {
	   HttpServletRequest request = 
	       ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	   String base = 
	      "http://" + request.getServerName() 
	      + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
	   return base;
	}
 	
 	private String getDataUrl(long videoId){
        return getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
    }
 	
  	public void saveSomeVideo(Video v, MultipartFile videoData) throws IOException {
  		videoDataMgr = VideoFileManager.get();
 	    videoDataMgr.saveVideoData(v, videoData.getInputStream());
 	}
 	
 	public void serveSomeVideo(Video v, HttpServletResponse response) throws IOException {
 	     // Of course, you would need to send some headers, etc. to the
 	     // client too!
 	     //  ...
 		videoDataMgr = VideoFileManager.get();
 	    videoDataMgr.copyVideoData(v, response.getOutputStream());
 	}
}
