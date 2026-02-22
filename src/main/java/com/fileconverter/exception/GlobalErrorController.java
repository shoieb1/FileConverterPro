package com.fileconverter.exception;

import com.fileconverter.service.ToolRegistryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class GlobalErrorController implements ErrorController {

    private final ToolRegistryService toolRegistry;

    public GlobalErrorController(ToolRegistryService toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusCode = request.getAttribute("jakarta.servlet.error.status_code");
        Object message = request.getAttribute("jakarta.servlet.error.message");
        Object uri = request.getAttribute("jakarta.servlet.error.request_uri");

        model.addAttribute("statusCode", statusCode != null ? statusCode : 500);
        model.addAttribute("message", message != null && !message.toString().isBlank()
                ? message : "Something went wrong.");
        model.addAttribute("uri", uri != null ? uri : "/");
        model.addAttribute("allTools", toolRegistry.getAllTools());
        model.addAttribute("toolsByCategory", toolRegistry.getToolsByCategory());
        return "error";
    }
}
