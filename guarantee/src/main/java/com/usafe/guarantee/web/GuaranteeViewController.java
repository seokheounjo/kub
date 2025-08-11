package com.usafe.guarantee.web;

import com.usafe.guarantee.domain.Guarantee;
import com.usafe.guarantee.service.GuaranteeService;
import com.usafe.guarantee.dto.GuaranteeForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class GuaranteeViewController {

    private final GuaranteeService service;

    @Value("${app.title}")
    private String appTitle;

    @Value("${app.version}")
    private String appVersion;

    @GetMapping({"/", "/guarantees", "/guarantees/list"})
    public String list(Model model) {
        List<Guarantee> list = service.list();
        model.addAttribute("title", "USAFE Guarantee – GitOps Delivery Demo");
        model.addAttribute("version", "0.0.2");
        model.addAttribute("guarantees", list);
        return "guarantees/list";  // /templates/guarantees/list.mustache
    }

    @GetMapping("/guarantees/new")
    public String newForm(Model model) {
        model.addAttribute("title", "USAFE Guarantee – GitOps Delivery Demo");
        model.addAttribute("version", "0.0.2");

        // 폼 초기값을 Map으로 내려서 Mustache가 100% 읽게 만든다
        java.util.Map<String, Object> emptyForm = new java.util.HashMap<>();
        emptyForm.put("orderId", "");
        emptyForm.put("buyerName", "");
        emptyForm.put("amount", "");

        model.addAttribute("form", emptyForm);
        return "guarantees/new";
    }




    @PostMapping("/guarantees")
    public String create(@Valid @ModelAttribute("form") GuaranteeForm form,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "USAFE Guarantee – GitOps Delivery Demo");
            model.addAttribute("version", "0.0.2");
            model.addAttribute("errors", bindingResult.getAllErrors()
                    .stream().map(e -> e.getDefaultMessage() == null ? "입력값을 확인하세요." : e.getDefaultMessage()).toList());
            return "guarantees/new";
        }
        Guarantee g = Guarantee.builder()
                .orderId(form.getOrderId())
                .buyerName(form.getBuyerName())
                .amount(form.getAmount())
                .build();
        service.issue(g);
        return "redirect:/guarantees/list";
    }

}
