package net.yangziwen.bookshelf.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@Entity
@Table(name="book")
public class Book {
	
	@Id
	@GeneratedValue
	@Column
	private Long id;
	@Column
	private String name;
	@Column
	private String authorName;
	@Column
	private String publisher;
	@Column
	private String isbn;
	@Column
	private String year;
	@Column
	private Integer pages;
	@Column
	private String language;
	@Column
	private String size;
	@Column
	private String format;
	@Column
	private String pageUrl;
	@Column
	private String downloadUrl;
	@Column
	private String coverImgUrl;
	@Column
	private String storagePath;
	
	public Book() {}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public String getIsbn() {
		return isbn;
	}
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public Integer getPages() {
		return pages;
	}
	public void setPages(Integer pages) {
		this.pages = pages;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getDownloadUrl() {
		return downloadUrl;
	}
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	public String getCoverImgUrl() {
		return coverImgUrl;
	}
	public void setCoverImgUrl(String coverImgUrl) {
		this.coverImgUrl = coverImgUrl;
	}
	public String getStoragePath() {
		return storagePath;
	}
	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getPageUrl() {
		return pageUrl;
	}

	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
