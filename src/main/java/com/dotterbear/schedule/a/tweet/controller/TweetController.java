package com.dotterbear.schedule.a.tweet.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.dotterbear.schedule.a.tweet.bus.service.TweetService;
import com.dotterbear.schedule.a.tweet.db.model.ScheduledTweet;
import com.dotterbear.schedule.a.tweet.db.model.UploadFile;
import com.dotterbear.schedule.a.tweet.exception.StorageFileNotFoundException;

@Controller
public class TweetController {

  @Autowired
  private TweetService tweetService;

  @Value("${com.dotterbear.format.request.tweet-date}")
  private String tweetRequestDateFormat;

  @GetMapping("/images/{uploadFileId}")
  @ResponseBody
  public ResponseEntity<Resource> getUploadImage(@PathVariable String uploadFileId) {
    Resource file = tweetService.getUploadImage(uploadFileId);
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"" + file.getFilename() + "\"").body(file);
  }

  @GetMapping("/")
  public String listUploadedFiles(Model model) {
    model.addAttribute("scheduledtweets", tweetService.findScheduledTweets());
    model.addAttribute("postedTweets", tweetService.findPostedTweets());
    if (!model.containsAttribute("scheduledTweet")) {
      model.addAttribute("scheduledTweet", new ScheduledTweet());
    }
    return "index";
  }

  @PostMapping("/add-tweet")
  public String addTweet(@ModelAttribute ScheduledTweet scheduledTweet,
      RedirectAttributes redirectAttributes) throws Exception {
    tweetService.addTweet(scheduledTweet);
    return "redirect:/";
  }

  @RequestMapping(value = "/add-media", method = RequestMethod.POST)
  public String addMedia(@ModelAttribute ScheduledTweet scheduledTweet,
      @RequestParam("tweetFile") MultipartFile tweetFile,
      RedirectAttributes redirectAttributes)
      throws Exception {
    UploadFile uploadFile = tweetService.addMedia(tweetFile);
    scheduledTweet.setUploadFileId(uploadFile.getId());
    redirectAttributes.addFlashAttribute("scheduledTweet", scheduledTweet);
    redirectAttributes.addFlashAttribute("uploadFile", uploadFile);
    return "redirect:/";
  }

  @ExceptionHandler(StorageFileNotFoundException.class)
  public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
    return ResponseEntity.notFound().build();
  }

  @InitBinder
  public void initDateBinder(final WebDataBinder binder) {
    binder.registerCustomEditor(Date.class,
        new CustomDateEditor(new SimpleDateFormat(tweetRequestDateFormat), true));
  }

}
