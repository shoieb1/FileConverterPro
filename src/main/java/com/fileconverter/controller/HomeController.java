package com.fileconverter.controller;

import com.fileconverter.service.StatsService;
import com.fileconverter.service.ToolRegistryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final ToolRegistryService toolRegistry;
    private final StatsService statsService;

    public HomeController(ToolRegistryService toolRegistry, StatsService statsService) {
        this.toolRegistry = toolRegistry;
        this.statsService = statsService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("tools", toolRegistry.getAllTools());
        model.addAttribute("popularTools", toolRegistry.getPopularTools());
        model.addAttribute("toolsByCategory", toolRegistry.getToolsByCategory());
        model.addAttribute("stats", statsService.getStats());
        return "index";
    }

    @GetMapping("/tool/{id}")
    public String tool(@PathVariable String id, Model model) {
        var tool = toolRegistry.getToolById(id);
        if (tool == null) return "redirect:/";
        model.addAttribute("tool", tool);
        model.addAttribute("allTools", toolRegistry.getAllTools());
        model.addAttribute("toolsByCategory", toolRegistry.getToolsByCategory());
        return "tool";
    }

    @GetMapping("/stats")
    public String stats(Model model) {
        model.addAttribute("stats", statsService.getStats());
        model.addAttribute("allTools", toolRegistry.getAllTools());
        model.addAttribute("toolsByCategory", toolRegistry.getToolsByCategory());
        return "stats";
    }

    @GetMapping("/thankyou")
    public String thankyou(@RequestParam(required = false) String tool, Model model) {
        model.addAttribute("toolName", tool);
        model.addAttribute("allTools", toolRegistry.getAllTools());
        model.addAttribute("toolsByCategory", toolRegistry.getToolsByCategory());
        return "thankyou";
    }
}
