package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
public class VideoController {

	@Autowired
	private VideoRepository videos;
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return Lists.newArrayList(videos.findAll());
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method=RequestMethod.GET)
	public @ResponseBody Video getVideoById(@PathVariable("id") long id) {
		return videos.findOne(id);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		return videos.save(v);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method=RequestMethod.POST)
	public void likeVideo(@PathVariable("id") long id, HttpServletResponse response, Principal p) {
		Video video = videos.findOne(id);
		if (video == null) {
			response.setStatus(404);
		}
		else {
			Set<String> likesUsernames = video.getLikesUsernames();
			if (!likesUsernames.contains(p.getName())) {
				boolean isAdded = likesUsernames.add(p.getName());
				if (isAdded) {
					video.setLikesUsernames(likesUsernames);
					video.setLikes(video.getLikes() + 1);
					videos.save(video);
				}
			}
			else {
				response.setStatus(400);
			}
		}
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method=RequestMethod.POST)
	public void unlikeVideo(@PathVariable("id") long id, HttpServletResponse response, Principal p) {
		Video video = videos.findOne(id);
		if (video == null) {
			response.setStatus(404);
		}
		else {
			Set<String> likesUsernames = video.getLikesUsernames();
			if (likesUsernames.contains(p.getName())) {
				boolean isRemoved = likesUsernames.remove(p.getName());
				if (isRemoved) {
					video.setLikesUsernames(likesUsernames);
					video.setLikes(video.getLikes() - 1);
					videos.save(video);
				}
			}
			else {
				response.setStatus(404);
			}
		}
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByTitle(@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title) {
		return Lists.newArrayList(videos.findByName(title));
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(@RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration) {
		return videos.findByDurationLessThan(duration);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method=RequestMethod.GET)
	public @ResponseBody Collection<String> getUsersWhoLikedVideo(@PathVariable("id") long id, HttpServletResponse response, Principal p) {
		Video video = videos.findOne(id);
		if (video == null) {
			response.setStatus(404);
		}
		else {
			return video.getLikesUsernames();
		}
		return null;
	}
}
