package javamid.vitrina.app.model;


public class Paging {
  private int pageNumber;
  private int pageSize;
  private boolean hasNext;
  private boolean hasPrevious;

  public Paging(){ pageNumber = 1; pageSize = 10; }

  public void setPageNumber( int pageNumber ){this.pageNumber=pageNumber;}
  public void setPageSize( int pageSize ){this.pageSize=pageSize;}
  public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
  public void setNext(boolean hasNext) { this.hasNext = hasNext; }
  public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }
  public void setPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }

  public int pageNumber() { return pageNumber; }
  public int pageSize()   { return pageSize; }
  public boolean hasNext() { return hasNext; }
  public boolean hasPrevious()   { return hasPrevious; }
}
